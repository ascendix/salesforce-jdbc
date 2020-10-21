package com.ascendix.jdbc.salesforce.connection;

import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import com.ascendix.jdbc.salesforce.metadata.ForceDatabaseMetaData;
import com.sforce.soap.partner.PartnerConnection;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class ForceConnection implements Connection {

    private final PartnerConnection partnerConnection;
    private final DatabaseMetaData metadata;
    private static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private Map connectionCache = new HashMap<>();
    Properties clientInfo = new Properties();

    public ForceConnection(PartnerConnection partnerConnection) {
        this.partnerConnection = partnerConnection;
        this.metadata = new ForceDatabaseMetaData(this);
    }

    public PartnerConnection getPartnerConnection() {
        return partnerConnection;
    }

    public DatabaseMetaData getMetaData() {
        return metadata;
    }

    @Override
    public PreparedStatement prepareStatement(String soql) {
        return new ForcePreparedStatement(this, soql);
    }

    @Override
    public String getSchema() {
        return "Salesforce";
    }

    public Map getCache() {
        return connectionCache;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Statement createStatement() {
        logger.info("[Conn] createStatement 1 IMPLEMENTED ");
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql) {
        logger.info("[Conn] prepareCall NOT_IMPLEMENTED "+sql);
        return null;
    }

    @Override
    public String nativeSQL(String sql) {
        logger.info("[Conn] nativeSQL NOT_IMPLEMENTED "+sql);
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void commit() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClosed() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isReadOnly() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCatalog() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
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
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        Logger.getLogger(SF_JDBC_DRIVER_NAME).info(Object.class.getEnclosingMethod().getName());
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // TODO Auto-generated method stub
        logger.info("[Conn] setClientInfo 1 IMPLEMENTED "+name+"="+value);
        clientInfo.setProperty(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        logger.info("[Conn] setClientInfo 2 IMPLEMENTED properties<>");
        properties.stringPropertyNames().forEach(propName -> clientInfo.setProperty(propName, properties.getProperty(propName)));
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        logger.info("[Conn] getClientInfo 1 IMPLEMENTED for '"+name+"'");
        return clientInfo.getProperty(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        logger.info("[Conn] getClientInfo 2 IMPLEMENTED ");
        return clientInfo;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }
}
