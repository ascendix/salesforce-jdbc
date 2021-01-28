package com.ascendix.jdbc.salesforce;

import com.ascendix.jdbc.salesforce.connection.ForceConnection;
import com.ascendix.jdbc.salesforce.metadata.ForceDatabaseMetaData;
import com.ascendix.jdbc.salesforce.metadata.Table;
import com.sforce.soap.partner.PartnerConnection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class ForceDriverTest {

    private ForceDriver driver;

    @Before
    public void setUp() {
        driver = new ForceDriver();
    }

    @Test
    public void testGetConnStringProperties() throws IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://prop1=val1;prop2=val2");

        assertEquals(2, actuals.size());
        assertEquals("val1", actuals.getProperty("prop1"));
        assertEquals("val2", actuals.getProperty("prop2"));
    }

    @Test
    public void testGetConnStringProperties_WhenNoValue() throws IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://prop1=val1; prop2; prop3 = val3");

        assertEquals(3, actuals.size());
        assertTrue(actuals.containsKey("prop2"));
        assertEquals("", actuals.getProperty("prop2"));
    }

    @Test
    public void testConnect_WhenWrongURL() throws  SQLException {
        Connection connection = driver.connect("jdbc:mysql://localhost/test", new Properties());

        assertNull(connection);
    }

    @Test
    @Ignore
    public void testConnect_WhenRightURL() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id from Account");
        ResultSet results = select_id_from_account1.executeQuery();
        assertNotNull(results);
    }

    @Test
    @Ignore
    public void testConnect_Insert() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("insert into Account(Name, OwnerId) values ('FirstAccount', '005xx1231231233123')");
        ResultSet results = select_id_from_account1.executeQuery();
        assertNotNull(results);
    }

    @Test
    @Ignore
    public void testConnect_Reconnect() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:6109?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id, Name from Account");
        ResultSet results = select_id_from_account1.executeQuery();
        System.out.println(renderResultSet(results));

        PreparedStatement reconnect = connection.prepareStatement("CONNECT USER admin@161866466774053.com IDENTIFIED by \"123456\"");
        results = reconnect.executeQuery();
        System.out.println(renderResultSet(results));
        assertNotNull(results);
        PreparedStatement select_id_from_account2 = connection.prepareStatement("select Id, Name from Account");
        results = select_id_from_account2.executeQuery();
        System.out.println(renderResultSet(results));
        assertNotNull(results);
    }

    @Test
    @Ignore
    public void testConnect_ReconnectToHost_FullJDBC() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:7357?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id, Name from Organization");
        ResultSet results = select_id_from_account1.executeQuery();
        System.out.println(renderResultSet(results));

        try {
            PreparedStatement reconnect = connection.prepareStatement("CONNECT TO  " +
                    " jdbc:ascendix:salesforce://admin@RecColl03.org:test12345@ap1.stmpa.stm.salesforce.com?https=true&api=51.0 ");
            results = reconnect.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
            PreparedStatement select_id_from_account2 = connection.prepareStatement("select Id, Name from Organization");
            results = select_id_from_account2.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
        } catch (Exception e) {
            throw new SQLException("Failed to log in into RecColl03 - using full jdbc URL", e);
        }
    }

    @Test
    @Ignore
    public void testConnect_ReconnectToHost_FullJDBC_WithUser() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:7357?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id, Name from Organization");
        ResultSet results = select_id_from_account1.executeQuery();
        System.out.println(renderResultSet(results));

        try {
            PreparedStatement reconnect3 = connection.prepareStatement("CONNECT TO  " +
                    " jdbc:ascendix:salesforce://admin@RecColl04.org:test12345@ap1.stmpa.stm.salesforce.com?https=true&api=51.0 "+
                    " USER CollectionOwner@RecColl04.org IDENTIFIED by \"test12345\"");
            results = reconnect3.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
            PreparedStatement select_id_from_account3 = connection.prepareStatement("select Id, Name from Organization");
            results = select_id_from_account3.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
        } catch (Exception e) {
            throw new SQLException("Failed to log in into RecColl04 - using full jdbc URL with conflicting User and password", e);
        }

    }

    @Test
    @Ignore
    public void testConnect_ReconnectToHost_ByHostName() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:7357?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id, Name from Organization");
        ResultSet results = select_id_from_account1.executeQuery();
        System.out.println(renderResultSet(results));

        try {
            PreparedStatement reconnect3 = connection.prepareStatement("CONNECT TO  " +
                    " https://ap1.stmpa.stm.salesforce.com "+
                    " USER CollectionOwner@RecColl04.org IDENTIFIED by \"test12345\"");
            results = reconnect3.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
            PreparedStatement select_id_from_account3 = connection.prepareStatement("select Id, Name from Organization");
            results = select_id_from_account3.executeQuery();
            System.out.println(renderResultSet(results));
            assertNotNull(results);
        } catch (Exception e) {
            throw new SQLException("Failed to log in into RecColl02 - using host name", e);
        }
    }

    @Test
    @Ignore
    public void testConnect_ReconnectToHost_Failed() throws  SQLException {
        Connection connection = driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:7357?https=false&api=48.0", new Properties());
        assertNotNull(connection);
        PreparedStatement select_id_from_account1 = connection.prepareStatement("select Id, Name from Organization");
        ResultSet results = select_id_from_account1.executeQuery();
        System.out.println(renderResultSet(results));

        try {
            PreparedStatement reconnect3 = connection.prepareStatement("CONNECT TO  " +
                    " https://ap1.stmpa.stm.salesforce.com "+
                    " USER CollectionOwner23@RecColl04.org IDENTIFIED by \"test12345\"");
            reconnect3.executeQuery();
            assertFalse("Should fail on the execution astep as user is wrong", true);
        } catch (SQLException e) {
            assertEquals("Expected message is wrong",
                    "CONNECTION ERROR as CollectionOwner23@RecColl04.org to ap1.stmpa.stm.salesforce.com : Relogin failed (INVALID_LOGIN) Invalid username, password, security token; or user locked out.",
                    e.getMessage());
        }
    }


    private String renderResultSet(ResultSet results) throws SQLException {
        StringBuilder out = new StringBuilder();

        int count = 0;
        int columnsCount = results.getMetaData().getColumnCount();

        // print header
        for(int i = 0; i < columnsCount; i++) {
            out.append(results.getMetaData().getColumnName(i+1)).append("\t");
        }
        out.append("\n");

        while(results.next()) {
            for(int i = 0; i < columnsCount; i++) {
                out.append(" " + results.getString(i+1)).append("\t");
            }
            out.append("\n");
            count++;
        }
        out.append("-----------------\n");
        out.append(count).append(" records\n");
        if (results.getWarnings() != null) {
            out.append("----------------- WARNINGS:\n");
            SQLWarning warning = results.getWarnings();
            while(warning != null) {
                out.append(warning.getMessage()).append("\n");
                warning = warning.getNextWarning();
            }
        }
        return out.toString();
    }

    @Test
    @Ignore
    public void testConnect_getTables() throws  SQLException {
//        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://admin@serega.org:Sergey251084@sprystupa-ltm.internal.salesforce.com:6101?https=true&api=52.0&insecurehttps=true", new Properties());
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://admin@serega.org:Sergey251084@sprystupa-wsm.internal.salesforce.com:7443?https=true&api=52.0&insecurehttps=true", new Properties());
//        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
//        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=50.0", new Properties());
//        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://demo@superstore.com:|4j!d12MBV@superstore.com:6109?https=false&api=51.0", new Properties());
        assertNotNull(connection);

        ForceDatabaseMetaData metaData = new ForceDatabaseMetaData(connection);
        ResultSet schemas = metaData.getSchemas();
        assertNotNull(schemas);
        ResultSet catalogs = metaData.getCatalogs();
        assertNotNull(catalogs);
        String[] types = null;
        ResultSet tables = metaData.getTables("catalog", "", "%", types);
        assertNotNull(tables);
        int count = 0;
        while(tables.next()) {
            System.out.println(" "+tables.getString("TABLE_NAME"));
            count++;
        }
        System.out.println(count+" Tables total");
    }

    @Test
    @Ignore
    public void testConnect_querySimple() throws  SQLException {
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        assertNotNull(statement);
        ResultSet resultSet = statement.executeQuery("select Id, Account.Name, Owner.id, Owner.Name from Account");
        assertNotNull(resultSet);
        assertNotNull("One record should be present", resultSet.first());
        resultSet.getMetaData();
    }

    @Test
    @Ignore
    public void testConnect_querySubSelect() throws  SQLException {
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        assertNotNull(statement);
        ResultSet resultSet = statement.executeQuery("select Id, name, Description, OwnerId, Group.Id, Group.Name, Group.OwnerId  from SharingRecordCollection LIMIT 1");
        assertNotNull(resultSet);
        assertEquals("One record should be present", resultSet.first());
        resultSet.getMetaData();
    }

    @Test
    @Ignore
    public void testConnect_querySelectWithUTF() throws  SQLException {
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        assertNotNull(statement);
        // 🙉𥿃🙂😂𣻰😯🗸🐌

        ResultSet resultSet = statement.executeQuery("select Id, Name from Account where Name like '\uD83D\uDE49\uD857\uDFC3\uD83D\uDE42\uD83D\uDE02\uD84F\uDEF0\uD83D\uDE2F\uD83D\uDDF8\uD83D\uDC0C'");
        assertNotNull(resultSet);
        assertEquals("One record should be present", resultSet.first());
        resultSet.getMetaData();
    }

    @Test
    @Ignore
    public void testConnect_querySelectWithQuotes() throws  SQLException {
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        assertNotNull(statement);

        ResultSet resultSet = statement.executeQuery("select \"Id\", \"IsDeleted\", \"Name\" from Account");
        assertNotNull(resultSet);
        assertEquals("One record should be present", resultSet.first());
        resultSet.getMetaData();
    }

    @Test
    public void testGetConnStringProperties_StandartUrlFormat() throws  IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://test@test.ru:aaaa!aaa@login.salesforce.ru:7642");

        assertEquals(3, actuals.size());
        assertTrue(actuals.containsKey("user"));
        assertEquals("test@test.ru", actuals.getProperty("user"));
        assertEquals("aaaa!aaa", actuals.getProperty("password"));
        assertEquals("login.salesforce.ru:7642", actuals.getProperty("loginDomain"));
    }

    @Test
    public void testGetConnStringProperties_HostName() throws  IOException {
        Properties actuals = driver.getConnStringProperties("login.salesforce.ru:7642");

        assertEquals(2, actuals.size());
        assertEquals("login.salesforce.ru:7642", actuals.getProperty("loginDomain"));
        assertEquals(true, driver.resolveBooleanProperty(actuals, "https", true));
    }

    @Test
    public void testGetConnStringProperties_HostNameHttp() throws  IOException {
        Properties actuals = driver.getConnStringProperties("http://login.salesforce.ru:7642");

        assertEquals(2, actuals.size());
        assertEquals("login.salesforce.ru:7642", actuals.getProperty("loginDomain"));
        assertEquals(false, driver.resolveBooleanProperty(actuals, "https", true));
    }

    @Test
    public void testGetConnStringProperties_IP() throws  IOException {
        Properties actuals = driver.getConnStringProperties("192.168.0.2:7642");

        assertEquals(2, actuals.size());
        assertEquals("192.168.0.2:7642", actuals.getProperty("loginDomain"));
        assertEquals(true, driver.resolveBooleanProperty(actuals, "https", true));
    }

    @Test
    public void testGetConnStringProperties_StandartUrlFormatHttpsApi() throws  IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://test@test.ru:aaaa!aaa@login.salesforce.ru?https=false&api=48.0");

        assertEquals(5, actuals.size());
        assertTrue(actuals.containsKey("user"));
        assertEquals("test@test.ru", actuals.getProperty("user"));
        assertEquals("aaaa!aaa", actuals.getProperty("password"));
        assertEquals("login.salesforce.ru", actuals.getProperty("loginDomain"));
        assertEquals("false", actuals.getProperty("https"));
        assertEquals("48.0", actuals.getProperty("api"));
    }
}
