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
    public void testGetConnStringProperties_ListNoHost() throws IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://prop1=val1;prop2=val2");

        assertEquals(2, actuals.size());
        assertEquals("val1", actuals.getProperty("prop1"));
        assertEquals("val2", actuals.getProperty("prop2"));
    }

    @Test
    public void testGetConnStringProperties_ListWithHost() throws IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://login.salesforce.ru:7642;prop1=val1;prop2=val2");

        assertEquals(3, actuals.size());
        assertEquals("login.salesforce.ru:7642", actuals.getProperty("loginDomain"));
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
    public void testGetConnStringProperties_StandartUrlFormat() throws  IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://test@test.ru:aaaa!aaa@login.salesforce.ru:7642");

        assertEquals(3, actuals.size());
        assertTrue(actuals.containsKey("user"));
        assertEquals("test@test.ru", actuals.getProperty("user"));
        assertEquals("aaaa!aaa", actuals.getProperty("password"));
        assertEquals("login.salesforce.ru:7642", actuals.getProperty("loginDomain"));
    }

    @Test
    public void testGetConnStringProperties_JdbcUrlFormatNoUser() throws  IOException {
        Properties actuals = driver.getConnStringProperties("jdbc:ascendix:salesforce://login.salesforce.ru:7642");

        assertEquals(1, actuals.size());
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
