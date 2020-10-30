package com.ascendix.jdbc.salesforce.connection;

import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import com.ascendix.jdbc.salesforce.metadata.ForceDatabaseMetaData;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

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
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForceConnection implements Connection {

    @FunctionalInterface
    public interface UpdateLoginFunction {

        /**
         * Applies this function to the given arguments.
         *
         * @param url the first function argument
         * @param user the second function argument
         * @param pass the second function argument
         * @return the function result
         */
        PartnerConnection apply(String url, String user, String pass);
    }

    private final PartnerConnection partnerConnection;
    /** the updated partner connection in case if we want to support relogin command */
    private PartnerConnection partnerConnectionUpdated;
    /** the function to provide partner connection in case if we want to support relogin command */
    UpdateLoginFunction loginHandler;

    private final DatabaseMetaData metadata;
    private static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private Map connectionCache = new HashMap<>();
    Properties clientInfo = new Properties();

    public ForceConnection(PartnerConnection partnerConnection, UpdateLoginFunction loginHandler) {
        this.partnerConnection = partnerConnection;
        this.metadata = new ForceDatabaseMetaData(this);
        this.loginHandler = loginHandler;
    }

    public PartnerConnection getPartnerConnection() {
        if (partnerConnectionUpdated != null) {
            return  partnerConnectionUpdated;
        }
        return partnerConnection;
    }

    public boolean updatePartnerConnection(String url, String userName, String userPass) {
        boolean result = false;
        String currentUserName = null;
        try {
            currentUserName = partnerConnection.getUserInfo().getUserName();
        } catch (ConnectionException e) {
        }
        logger.info("[Conn] updatePartnerConnection IMPLEMENTED newUserName="+userName + " oldUserName="+currentUserName + " newUrl="+url);
        if (loginHandler != null) {
            try {
                PartnerConnection newPartnerConnection = loginHandler.apply(url, userName, userPass);
                if (newPartnerConnection != null) {
                    partnerConnectionUpdated = newPartnerConnection;
                    logger.info("[Conn] updatePartnerConnection UPDATED to newUserName="+userName);
                    result = true;
                } else {
                    logger.log(Level.SEVERE, "[Conn] updatePartnerConnection UPDATE FAILED to newUserName="+userName+" currentUserName="+currentUserName);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[Conn] updatePartnerConnection UPDATE FAILED to newUserName="+userName+" currentUserName="+currentUserName, e);
            }
        }
        return result;
    }

    public DatabaseMetaData getMetaData() {
        return metadata;
    }

    @Override
    public PreparedStatement prepareStatement(String soql) {
        logger.info("[Conn] prepareStatement IMPLEMENTED "+soql);
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
        return new ForcePreparedStatement(this);
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
        return true;
    }

    @Override
    public void commit() throws SQLException {
        // TODO Auto-generated method stub
        logger.info("[Conn] commit NOT_IMPLEMENTED ");
    }

    @Override
    public void rollback() throws SQLException {
        // TODO Auto-generated method stub
        logger.info("[Conn] rollback NOT_IMPLEMENTED ");
    }

    @Override
    public void close() throws SQLException {
        // TODO Auto-generated method stub
        logger.info("[Conn] close NOT_IMPLEMENTED ");
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
        logger.info("[Conn] setCatalog NOT_IMPLEMENTED set to '"+catalog+"'");
    }

    @Override
    public String getCatalog() throws SQLException {
        logger.info("[Conn] getCatalog IMPLEMENTED returning "+ForceDatabaseMetaData.DEFAULT_CATALOG);
        return ForceDatabaseMetaData.DEFAULT_CATALOG;
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
        logger.info("[Conn] createStatement 2 IMPLEMENTED");
        return new ForcePreparedStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        logger.info("[Conn] prepareStatement 1 IMPLEMENTED "+sql);
        return new ForcePreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        logger.info("[Conn] prepareCall NOT_IMPLEMENTED "+sql);
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        logger.info("[Conn] getTypeMap NOT_IMPLEMENTED ");
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
        logger.info("[Conn] rollback Savepoint NOT_IMPLEMENTED");

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        logger.info("[Conn] releaseSavepoint NOT_IMPLEMENTED");

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        logger.info("[Conn] createStatement 3 NOT_IMPLEMENTED");
        return new ForcePreparedStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        logger.info("[Conn] prepareStatement 2 NOT_IMPLEMENTED "+sql );
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        logger.info("[Conn] prepareCall 2 NOT_IMPLEMENTED "+sql );
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        logger.info("[Conn] prepareStatement 3 NOT_IMPLEMENTED "+sql );
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        logger.info("[Conn] prepareStatement 4 NOT_IMPLEMENTED "+sql );
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        logger.info("[Conn] prepareStatement 5 NOT_IMPLEMENTED "+sql );
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
        logger.info("[Conn] isValid NOT_IMPLEMENTED ");
        return true;
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
        logger.info("[Conn] setSchema NOT_IMPLEMENTED ");
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
