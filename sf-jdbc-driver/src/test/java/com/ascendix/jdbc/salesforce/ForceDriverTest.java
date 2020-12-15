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

        PreparedStatement reconnect = connection.prepareStatement("CONNECT USER admin@15542148823767.com IDENTIFIED by \"123456\"");
        results = reconnect.executeQuery();
        System.out.println(renderResultSet(results));
        assertNotNull(results);
        PreparedStatement select_id_from_account2 = connection.prepareStatement("select Id, Name from Account");
        results = select_id_from_account2.executeQuery();
        System.out.println(renderResultSet(results));
        assertNotNull(results);
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
        ForceConnection connection = (ForceConnection)driver.connect("jdbc:ascendix:salesforce://dev@Local.org:123456@spuliaiev-wsm1.internal.salesforce.com:6109?https=false&api=51.0", new Properties());
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
    public void testGetConnStringProperties_StandartUrlFormat() throws  IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://test@test.ru:aaaa!aaa@login.salesforce.ru");

        assertEquals(3, actuals.size());
        assertTrue(actuals.containsKey("user"));
        assertEquals("test@test.ru", actuals.getProperty("user"));
        assertEquals("aaaa!aaa", actuals.getProperty("password"));
        assertEquals("login.salesforce.ru", actuals.getProperty("loginDomain"));
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
