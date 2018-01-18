package com.ascendix.jdbc.salesforce;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.rowset.RowSetMetaDataImpl;

import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mule.tools.soql.exception.SOQLParsingException;

import com.ascendix.jdbc.salesforce.SoqlQueryAnalyzer.FieldDef;
import com.ascendix.jdbc.salesforce.delegates.ForceResultField;
import com.ascendix.jdbc.salesforce.delegates.PartnerService;
import com.sforce.ws.ConnectionException;

//@SuppressWarnings({"rawtypes", "unchecked"})
public class ForcePreparedStatement implements PreparedStatement {

    private final static String CACHE_HINT = "(?is)\\A\\s*(CACHE\\s*(GLOBAL|SESSION)).*";

    protected enum CacheMode {
	NO_CACHE, GLOBAL, SESSION
    };

    private final static Serializer<Future<ResultSet>> FUTURE_RESULT_SET_SERIALIZER = new Serializer<Future<ResultSet>>() {
        
        @SuppressWarnings("unchecked")
	@Override
        public void serialize(DataOutput2 out, Future<ResultSet> value) throws IOException {
    		try {
		    Serializer.ELSA.serialize(out, value.get());
		} catch (InterruptedException | ExecutionException e) {
		    throw new IOException(e);
		}
        }
        
        @Override
        public Future<ResultSet> deserialize(DataInput2 in, int available) throws IOException {
            ResultSet result = (ResultSet) Serializer.ELSA.deserialize(in, available);
            return CompletableFuture.completedFuture(result);
        }
        
    };
    
    private String soqlQuery;
    private ForceConnection connection;
    private PartnerService partnerService;
    private ResultSetMetaData metadata;// = new RowSetMetaDataImpl();
    private int fetchSize;
    private int maxRows;
    private List<Object> parameters = new ArrayList<>();
    private CacheMode cacheMode = CacheMode.NO_CACHE;
    private static DB cacheDb = DBMaker.tempFileDB().closeOnJvmShutdown().make();
    
    //TODO: Join caches and move it to ForceConnection class. Divide to session and static global cache.
    private static HTreeMap<String, ResultSet> dataCache = cacheDb
	    .hashMap("DataCache", Serializer.STRING, Serializer.ELSA)
	    .expireAfterCreate(60, TimeUnit.MINUTES)
	    .expireStoreSize(16 * 1024*1024*1024)
	    .create();
    private static HTreeMap<String, ResultSetMetaData> metadataCache = cacheDb
	    .hashMap("MetadataCache", Serializer.STRING, Serializer.ELSA)
	    .expireAfterCreate(60, TimeUnit.MINUTES)
	    .expireStoreSize(1 * 1024*1024*1024)
	    .create();
    
    
    public ForcePreparedStatement(ForceConnection connection, String soql) {
	this.connection = connection;
	if (isContainsCacheHint(soql)) {
	    cacheMode = getCacheMode(soql);
	    soql = removeCacheHints(soql);
	}
	this.soqlQuery = soql;
    }

    public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
	    throw (T) throwable; // rely on vacuous cast
	}
    
    public ResultSet executeQuery() throws SQLException {
	return cacheMode == CacheMode.NO_CACHE
		? query()
		: dataCache.computeIfAbsent(getCacheKey(), s -> {
		    try {
			return query();
		    } catch (SQLException e) {
			rethrow(e);
			return null;
		    }
		});
    }    
    
    private ResultSet query() throws SQLException {
	try {
	    String preparedSoql = prepareQuery();
	    List<List> forceQueryResult = getPartnerService().query(preparedSoql, getFieldDefinitions());
	    if (!forceQueryResult.isEmpty()) {
		List<ColumnMap<String, Object>> maps = Collections.synchronizedList(new LinkedList<>());
		forceQueryResult.stream()
			.forEach(record -> maps.add(convertToColumnMap(record)));
		return new CachedResultSet(maps, getMetaData());
	    } else {
		return new CachedResultSet(Collections.emptyList(), getMetaData());
	    }
	} catch (ConnectionException | SOQLParsingException e) {
	    throw new SQLException(e);
	}
    }

    private String prepareQuery() {
	return addFetchSize(setParams(soqlQuery));
    }

    private ColumnMap<String, Object> convertToColumnMap(List<ForceResultField> record) {
	ColumnMap<String, Object> columnMap = new ColumnMap<String, Object>();
	record.stream()
		.map(field -> field == null ? new ForceResultField(null, null, null, null) : field)
		.forEach(field -> {
		    columnMap.put(field.getFullName(), field.getValue());
		});
	return columnMap;
    }

    protected String removeCacheHints(String query) {
	Matcher matcher = Pattern.compile(CACHE_HINT).matcher(query);
	if (matcher.matches()) {
	    String hint = matcher.group(1);
	    return query.replaceFirst(hint, "");
	} else {
	    return query;
	}
    }

    protected CacheMode getCacheMode(String query) {
	Matcher matcher = Pattern.compile(CACHE_HINT).matcher(query);
	if (matcher.matches()) {
	    String mode = matcher.group(2);
	    return CacheMode.valueOf(mode.toUpperCase());
	} else {
	    return CacheMode.NO_CACHE;
	}
    }

    protected boolean isContainsCacheHint(String query) {
	return query.matches(CACHE_HINT);
    }

    private String getCacheKey() throws SQLException {
	try {
	    String preparedQuery = prepareQuery();
	    return cacheMode == CacheMode.GLOBAL
	    	? preparedQuery
	    	: connection.getPartnerConnection().getSessionHeader().getSessionId() + preparedQuery;
	} catch (ConnectionException e) {
	    throw new SQLException(e);
	}
    }

    private String addFetchSize(String soql) {
	return fetchSize > 0
		? soql += " LIMIT " + fetchSize
		: soql;
    }

    private List<? extends Object> getParameters() {
	int paramsCountInQuery = StringUtils.countMatches(soqlQuery, '?');
	if (parameters.size() < paramsCountInQuery) {
	    parameters.addAll(Collections.nCopies(paramsCountInQuery - parameters.size(), null));
	}
	return parameters;
    }
    
    protected String setParams(String soql) {
	String result = soql;
	for (Object param : getParameters()) {
	    String paramRepresentation = convertToSoqlParam(param);
	    result = result.replaceFirst("\\?", paramRepresentation);
	}
	return result;
    }

    private final static DateFormat SF_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static Map<Class<?>, Function<Object, String>> paramConverters = new HashMap<>();
    static {
	paramConverters.put(String.class, ForcePreparedStatement::toSoqlStringParam);
	paramConverters.put(Object.class, ForcePreparedStatement::toSoqlStringParam);
	paramConverters.put(Boolean.class, p -> p.toString());
	paramConverters.put(Double.class, p -> p.toString());
	paramConverters.put(BigDecimal.class, p -> p.toString());
	paramConverters.put(Float.class, p -> p.toString());
	paramConverters.put(Integer.class, p -> p.toString());
	paramConverters.put(Long.class, p -> p.toString());
	paramConverters.put(Short.class, p -> p.toString());
	paramConverters.put(java.util.Date.class, p -> SF_DATETIME_FORMATTER.format(p));
	paramConverters.put(Timestamp.class, p -> SF_DATETIME_FORMATTER.format(p));
	paramConverters.put(null, p -> "NULL");
    }

    protected static String toSoqlStringParam(Object param) {
	return "'" + param.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'";
    }

    protected static String convertToSoqlParam(Object paramValue) {
	Class<?> paramClass = getParamClass(paramValue);
	return paramConverters.get(paramClass).apply(paramValue);
    }

    protected static Class<?> getParamClass(Object paramValue) {
	Class<?> paramClass = paramValue != null ? paramValue.getClass() : null;
	if (!paramConverters.containsKey(paramClass)) {
	    paramClass = Object.class;
	}
	return paramClass;
    }

    private ResultSetMetaData loadMetaData() throws SQLException {
	try {
	    if (metadata == null) {
		RowSetMetaDataImpl result = new RowSetMetaDataImpl();
		SoqlQueryAnalyzer queryAnalyzer = getQueryAnalyzer();
		List<SoqlQueryAnalyzer.FieldDef> resultFieldDefinitions = flatten(getFieldDefinitions());
		int columnsCount = resultFieldDefinitions.size();
		result.setColumnCount(columnsCount);
		for (int i = 1; i <= columnsCount; i++) {
		    SoqlQueryAnalyzer.FieldDef field = resultFieldDefinitions.get(i - 1);
		    result.setAutoIncrement(i, false);
		    result.setColumnName(i, field.getName());
		    result.setColumnLabel(i, field.getName());
		    String forceTypeName = field.getType();
		    ForceDatabaseMetaData.TypeInfo typeInfo = ForceDatabaseMetaData.lookupTypeInfo(forceTypeName);
		    result.setColumnType(i, typeInfo.sqlDataType);
		    result.setColumnTypeName(i, typeInfo.typeName);
		    result.setPrecision(i, typeInfo.precision);
		    result.setSchemaName(i, "Salesforce");
		    result.setTableName(i, queryAnalyzer.getFromObjectName());
		}
		metadata = result;
	    }
	    return metadata;
	} catch (RuntimeException e) {
	    throw new SQLException(e.getCause() != null ? e.getCause() : e);
	}
    }

    private List<FieldDef> flatten(List fieldDefinitions) {
	return (List<FieldDef>) fieldDefinitions.stream()
		.flatMap(def -> def instanceof List 
			? ((List) def).stream() 
			: Arrays.asList(def).stream())
		.collect(Collectors.toList());
    }
    
    private List<FieldDef> fieldDefinitions;
    
    private List<FieldDef> getFieldDefinitions() {
	if (fieldDefinitions == null) {
	    fieldDefinitions = getQueryAnalyzer().getFieldDefinitions();
	}
	return fieldDefinitions;
    }

    private SoqlQueryAnalyzer queryAnalyzer;
    
    private SoqlQueryAnalyzer getQueryAnalyzer() {
	if (queryAnalyzer == null) {
	    queryAnalyzer = new SoqlQueryAnalyzer(prepareQuery(), (objName) -> {
		try {
		    return getPartnerService().describeSObject(objName);
		} catch (ConnectionException e) {
		    throw new RuntimeException(e);
		}
	    }, connection.getCache());
	}
	return queryAnalyzer;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
	return new ParameterMetadataImpl(parameters, soqlQuery);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
	return cacheMode == CacheMode.NO_CACHE
		? loadMetaData()
		: metadataCache.computeIfAbsent(getCacheKey(), s -> {
		    try {
			return loadMetaData();
		    } catch (SQLException e) {
			rethrow(e);
			return null;
		    }
		});
    }
    
    private PartnerService getPartnerService() throws ConnectionException {
	if (partnerService == null) {
	    partnerService = new PartnerService(connection.getPartnerConnection());
	}
	return partnerService;
    }

    public void setFetchSize(int rows) throws SQLException {
	this.fetchSize = rows;
    }

    public int getFetchSize() throws SQLException {
	return fetchSize;
    }

    public void setMaxRows(int max) throws SQLException {
	this.maxRows = max;
    }

    public int getMaxRows() throws SQLException {
	return maxRows;
    }

    public void setArray(int i, Array x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
	    throws SQLException {
	addParameter(parameterIndex, x);
    }

    protected void addParameter(int parameterIndex, Object x) {
	parameterIndex--;
	if (parameters.size() < parameterIndex) {
	    parameters.addAll(Collections.nCopies(parameterIndex - parameters.size(), null));
	}
	parameters.add(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setBlob(int i, Blob x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setClob(int i, Clob x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
	addParameter(parameterIndex, null);
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
	    throws SQLException {
	addParameter(paramIndex, null);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
	    int scale) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setRef(int i, Ref x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setTimestamp(int parameterIndex, Timestamp x)
	    throws SQLException {
	addParameter(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");

    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
	    throws SQLException {
	String methodName = this.getClass().getSimpleName() + "." + new Object() {
	}.getClass().getEnclosingMethod().getName();
	throw new UnsupportedOperationException("The " + methodName + " is not implemented yet.");
    }

    // Not required to implement below

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
	throw new RuntimeException("Not yet implemented.");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void close() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public int getQueryTimeout() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void cancel() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setCursorName(String name) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean execute(String sql) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public int getFetchDirection() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void clearBatch() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public int[] executeBatch() throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean isPoolable() throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public int executeUpdate() throws SQLException {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void clearParameters() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public boolean execute() throws SQLException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void addBatch() throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	// TODO Auto-generated method stub

    }

}
