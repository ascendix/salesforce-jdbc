package com.ascendix.jdbc.salesforce;

import javax.sql.rowset.serial.SerialBlob;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CachedResultSet implements ResultSet, Serializable {

    private static final long serialVersionUID = 1L;

    private transient Integer index;
    private List<ColumnMap<String, Object>> rows;
    private ResultSetMetaData metadata;

    public CachedResultSet(List<ColumnMap<String, Object>> rows) {
        this.rows = rows;
    }

    public CachedResultSet(List<ColumnMap<String, Object>> rows, ResultSetMetaData metadata) {
        this(rows);
        this.metadata = metadata;
    }

    public CachedResultSet(ColumnMap<String, Object> singleRow) {
        this(Arrays.asList(singleRow));
    }

    public Object getObject(String columnName) throws SQLException {
        return rows.get(getIndex()).get(columnName.toUpperCase());
    }

    public Object getObject(int columnIndex) throws SQLException {
        return rows.get(getIndex()).getByIndex(columnIndex);
    }

    private int getIndex() {
        if (index == null) {
            index = -1;
        }
        return index;
    }

    private void setIndex(int i) {
        index = i;
    }

    private void increaseIndex() {
        index = getIndex() + 1;
    }

    public String getString(String columnName) throws SQLException {
        return (String) getObject(columnName);
    }

    public String getString(int columnIndex) throws SQLException {
        return (String) getObject(columnIndex);
    }

    public boolean first() throws SQLException {
        if (rows.size() > 0) {
            setIndex(0);
            return true;
        } else {
            return false;
        }
    }

    public boolean last() throws SQLException {
        if (rows.size() > 0) {
            setIndex(rows.size() - 1);
            return true;
        } else {
            return false;
        }
    }

    public boolean next() throws SQLException {
        if (rows.size() > 0) {
            increaseIndex();
            return getIndex() < rows.size();
        } else {
            return false;
        }
    }

    public boolean isAfterLast() throws SQLException {
        return rows.size() > 0 && getIndex() == rows.size();
    }

    public boolean isBeforeFirst() throws SQLException {
        return rows.size() > 0 && getIndex() == -1;
    }

    public boolean isFirst() throws SQLException {
        return rows.size() > 0 && getIndex() == 0;
    }

    public boolean isLast() throws SQLException {
        return rows.size() > 0 && getIndex() == rows.size() - 1;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return metadata != null ? metadata : new CachedResultSetMetaData();
    }

    public void setFetchSize(int rows) throws SQLException {
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private class ColumnValueParser<T> {

        private Function<String, T> conversion;

        public ColumnValueParser(Function<String, T> parser) {
            this.conversion = parser;
        }

        public Optional<T> parse(int columnIndex) {
            Object value = rows.get(getIndex()).getByIndex(columnIndex);
            return parse(value);
        }

        public Optional<T> parse(String columnName) {
            Object value = rows.get(getIndex()).get(columnName.toUpperCase());
            return parse(value);
        }

        private Optional<T> parse(Object o) {
            if (o == null) return Optional.empty();
            if (!(o instanceof String)) return (Optional<T>) Optional.of(o);
            return Optional.of(conversion.apply((String) o));
        }

    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(BigDecimal::new)
                .parse(columnIndex)
                .orElse(null);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return new ColumnValueParser<>(BigDecimal::new)
                .parse(columnName)
                .orElse(null);
    }

    protected java.util.Date parseDate(String dateRepr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateRepr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Date getDate(int columnIndex) throws SQLException {
        return new ColumnValueParser<java.util.Date>((v) -> parseDate((String) v))
                .parse(columnIndex)
                .map(d -> new java.sql.Date(d.getTime()))
                .orElse(null);
    }

    public Date getDate(String columnName) throws SQLException {
        return new ColumnValueParser<>((v) -> parseDate(v))
                .parse(columnName)
                .map(d -> new java.sql.Date(d.getTime()))
                .orElse(null);
    }

    private java.util.Date parseDateTime(String dateRepr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(dateRepr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        Object value = rows.get(getIndex()).getByIndex(columnIndex);
        if (value instanceof GregorianCalendar) {
            return new java.sql.Timestamp(((GregorianCalendar) value).getTime().getTime());
        } else {
            return new ColumnValueParser<>((v) -> parseDateTime(v))
                    .parse(columnIndex)
                    .map(d -> new java.sql.Timestamp(d.getTime()))
                    .orElse(null);
        }
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        Object value = rows.get(getIndex()).get(columnName);
        if (value instanceof GregorianCalendar) {
            return new java.sql.Timestamp(((GregorianCalendar) value).getTime().getTime());
        } else {
            return new ColumnValueParser<>((v) -> parseDateTime(v))
                    .parse(columnName)
                    .map(d -> new java.sql.Timestamp(d.getTime()))
                    .orElse(null);
        }
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private java.util.Date parseTime(String dateRepr) {
        try {
            return new SimpleDateFormat("HH:mm:ss.SSSX").parse(dateRepr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Time getTime(String columnName) throws SQLException {
        return new ColumnValueParser<>((v) -> parseTime(v))
                .parse(columnName)
                .map(d -> new Time(d.getTime()))
                .orElse(null);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return new ColumnValueParser<>((v) -> parseTime(v))
                .parse(columnIndex)
                .map(d -> new Time(d.getTime()))
                .orElse(null);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) {
        Optional<BigDecimal> result = new ColumnValueParser<>(BigDecimal::new)
                .parse(columnIndex);
        result.ifPresent(v -> v.setScale(scale));
        return result.orElse(null);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) {
        Optional<BigDecimal> result = new ColumnValueParser<>(BigDecimal::new)
                .parse(columnName);
        result.ifPresent(v -> v.setScale(scale));
        return result.orElse(null);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Float::new)
                .parse(columnIndex)
                .orElse(0f);
    }

    public float getFloat(String columnName) throws SQLException {
        return new ColumnValueParser<>(Float::new)
                .parse(columnName)
                .orElse(0f);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Double::new)
                .parse(columnIndex)
                .orElse(0d);
    }

    public double getDouble(String columnName) throws SQLException {
        return new ColumnValueParser<>(Double::new)
                .parse(columnName)
                .orElse(0d);
    }

    public long getLong(String columnName) throws SQLException {
        return new ColumnValueParser<>(Long::new)
                .parse(columnName)
                .orElse(0L);
    }

    public long getLong(int columnIndex) throws SQLException {
        return new ColumnValueParser<Long>(Long::new)
                .parse(columnIndex)
                .orElse(0L);
    }

    public int getInt(String columnName) throws SQLException {
        return new ColumnValueParser<>(Integer::new)
                .parse(columnName)
                .orElse(0);
    }

    public int getInt(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Integer::new)
                .parse(columnIndex)
                .orElse(0);
    }

    public short getShort(String columnName) throws SQLException {
        return new ColumnValueParser<>(Short::new)
                .parse(columnName)
                .orElse((short) 0);
    }

    public short getShort(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Short::new)
                .parse(columnIndex)
                .orElse((short) 0);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private Blob createBlob(byte[] data) {
        try {
            return new SerialBlob(data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return new ColumnValueParser<>((v) -> Base64.getDecoder().decode(v))
                .parse(columnIndex)
                .map(this::createBlob)
                .orElse(null);
    }

    public Blob getBlob(String columnName) throws SQLException {
        return new ColumnValueParser<>((v) -> Base64.getDecoder().decode(v))
                .parse(columnName)
                .map(this::createBlob)
                .orElse(null);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Boolean::new)
                .parse(columnIndex)
                .orElse(false);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return new ColumnValueParser<>(Boolean::new)
                .parse(columnName)
                .orElse(false);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return new ColumnValueParser<>(Byte::new)
                .parse(columnIndex)
                .orElse((byte) 0);
    }

    public byte getByte(String columnName) throws SQLException {
        return new ColumnValueParser<>(Byte::new)
                .parse(columnName)
                .orElse((byte) 0);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public byte[] getBytes(String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    //
    // Not implemented below here
    //

    public boolean absolute(int row) throws SQLException {
        return false;
    }

    public void afterLast() throws SQLException {
        System.out.println("after last check");
    }

    public void beforeFirst() throws SQLException {
    }

    public void cancelRowUpdates() throws SQLException {
    }

    public void clearWarnings() throws SQLException {

    }

    public void close() throws SQLException {

    }

    public void deleteRow() throws SQLException {

    }

    public int findColumn(String columnName) throws SQLException {

        return 0;
    }

    public Array getArray(int i) throws SQLException {

        return null;
    }

    public Array getArray(String colName) throws SQLException {

        return null;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {

        return null;
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {

        return null;
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    public Reader getCharacterStream(String columnName) throws SQLException {

        return null;
    }

    public Clob getClob(int i) throws SQLException {

        return null;
    }

    public Clob getClob(String colName) throws SQLException {

        return null;
    }

    public int getConcurrency() throws SQLException {

        return 0;
    }

    public String getCursorName() throws SQLException {

        return null;
    }

    public int getFetchDirection() throws SQLException {

        return 0;
    }

    public int getFetchSize() throws SQLException {

        return 0;
    }

    public Object getObject(int i, Map<String, Class<?>> map)
            throws SQLException {

        return null;
    }

    public Object getObject(String colName, Map<String, Class<?>> map)
            throws SQLException {

        return null;
    }

    public Ref getRef(int i) throws SQLException {

        return null;
    }

    public Ref getRef(String colName) throws SQLException {

        return null;
    }

    public int getRow() throws SQLException {

        return 0;
    }

    public Statement getStatement() throws SQLException {

        return null;
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {

        return null;
    }

    public int getType() throws SQLException {

        return 0;
    }

    public URL getURL(int columnIndex) throws SQLException {

        return null;
    }

    public URL getURL(String columnName) throws SQLException {

        return null;
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {

        return null;
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {

        return null;
    }

    public SQLWarning getWarnings() throws SQLException {

        return null;
    }

    public void insertRow() throws SQLException {
    }

    public void moveToCurrentRow() throws SQLException {
    }

    public void moveToInsertRow() throws SQLException {
    }

    public boolean previous() throws SQLException {

        return false;
    }

    public void refreshRow() throws SQLException {
    }

    public boolean relative(int rows) throws SQLException {

        return false;
    }

    public boolean rowDeleted() throws SQLException {

        return false;
    }

    public boolean rowInserted() throws SQLException {

        return false;
    }

    public boolean rowUpdated() throws SQLException {

        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
    }

    public void updateArray(String columnName, Array x) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException {
    }

    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws SQLException {
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException {
    }

    public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException {
    }

    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws SQLException {
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
    }

    public void updateByte(String columnName, byte x) throws SQLException {
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException {
    }

    public void updateCharacterStream(String columnName, Reader reader,
                                      int length) throws SQLException {
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
    }

    public void updateDate(String columnName, Date x) throws SQLException {
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
    }

    public void updateDouble(String columnName, double x) throws SQLException {
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
    }

    public void updateFloat(String columnName, float x) throws SQLException {
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
    }

    public void updateInt(String columnName, int x) throws SQLException {
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
    }

    public void updateLong(String columnName, long x) throws SQLException {
    }

    public void updateNull(int columnIndex) throws SQLException {
    }

    public void updateNull(String columnName) throws SQLException {
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
    }

    public void updateObject(String columnName, Object x) throws SQLException {
    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException {
    }

    public void updateObject(String columnName, Object x, int scale)
            throws SQLException {
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
    }

    public void updateRow() throws SQLException {
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
    }

    public void updateShort(String columnName, short x) throws SQLException {
    }

    public void updateString(int columnIndex, String x) throws SQLException {
    }

    public void updateString(String columnName, String x) throws SQLException {
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
    }

    public void updateTime(String columnName, Time x) throws SQLException {
    }

    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException {
    }

    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException {
    }

    public boolean wasNull() throws SQLException {

        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {

        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

    public RowId getRowId(int columnIndex) throws SQLException {

        return null;
    }

    public RowId getRowId(String columnLabel) throws SQLException {

        return null;
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
    }

    public int getHoldability() throws SQLException {

        return 0;
    }

    public boolean isClosed() throws SQLException {

        return false;
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    }

    public NClob getNClob(int columnIndex) throws SQLException {

        return null;
    }

    public NClob getNClob(String columnLabel) throws SQLException {

        return null;
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {

        return null;
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {

        return null;
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    }

    public String getNString(int columnIndex) throws SQLException {

        return null;
    }

    public String getNString(String columnLabel) throws SQLException {

        return null;
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {

        return null;
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
