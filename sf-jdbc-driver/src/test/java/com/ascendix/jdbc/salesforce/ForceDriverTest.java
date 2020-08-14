package com.ascendix.jdbc.salesforce;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
