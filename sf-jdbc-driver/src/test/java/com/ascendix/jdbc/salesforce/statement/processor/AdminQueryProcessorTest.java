package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

public class AdminQueryProcessorTest {

    @Test
    public void testConnectORA_isAdminQuery() {
        assertFalse(AdminQueryProcessor.isAdminQuery(""));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN dev@Local.org/123456"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT dev@Local.org/123456"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN dev@Local.org/123456;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT dev@Local.org/123456;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN 'dev@Local.org'/'123456'"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT \"dev@Local.org\"/\"123456\""));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN dev@Local.org/123456;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT dev@Local.org/123456;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN 'dev@Local.org'/'123456';"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT \"dev@Local.org\"/\"123456\";"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONN 'dev@Local.org/123456';"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT \"dev@Local.org/123456\";"));
    }

    @Test
    public void testConnectPG_isAdminQuery_NoTO() {
        assertFalse(AdminQueryProcessor.isAdminQuery(""));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER dev@Local.org IDENTIFIED BY 123456"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER dev@Local.org IDENTIFIED BY 123456;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER 'dev@Local.org' IDENTIFIED BY '123456'"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER 'dev@Local.org' IDENTIFIED BY '123456';"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER \"dev@Local.org\" IDENTIFIED BY \"123456\""));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT USER \"dev@Local.org\" IDENTIFIED BY \"123456\";"));
    }

    @Test
    public void testConnectPG_isAdminQuery_WithTo() {
        assertFalse(AdminQueryProcessor.isAdminQuery(""));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT TO http://localhost:6109"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT TO http://localhost:6109 USER dev@Local.org IDENTIFIED BY 123456"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT TO http://localhost:6109;"));
        assertTrue(AdminQueryProcessor.isAdminQuery("CONNECT TO http://localhost:6109 USER dev@Local.org IDENTIFIED BY 123456;"));
    }

    @Test
    public void testConnectORA_processLoginCommand() throws SQLException {
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN dev@Local.org/123456", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT dev@Local.org/123456", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN dev@Local.org/123456;", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT dev@Local.org/123456;", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN 'dev@Local.org'/'123456'", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT \"dev@Local.org\"/\"123456\"", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN dev@Local.org/123456;", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT dev@Local.org/123456;", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN 'dev@Local.org'/'123456';", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT \"dev@Local.org\"/\"123456\";", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONN 'dev@Local.org/123456';", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT \"dev@Local.org/123456\";", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
    }
    @Test
    public void testConnectPG_processLoginCommand_NoTO() throws SQLException {
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER dev@Local.org IDENTIFIED BY 123456", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER dev@Local.org IDENTIFIED BY 123456;", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER 'dev@Local.org' IDENTIFIED BY '123456'", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER 'dev@Local.org' IDENTIFIED BY '123456';", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER \"dev@Local.org\" IDENTIFIED BY \"123456\"", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT USER \"dev@Local.org\" IDENTIFIED BY \"123456\";", null,
                testProcessor(null, "dev@Local.org", "123456", null, true)));
    }

    @Test
    public void testConnectPG_processLoginCommand_WithTo() throws SQLException {
        assertFalse(AdminQueryProcessor.processLoginCommand("", null, null));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT TO http://localhost:6109", null,
                testProcessor("http://localhost:6109", null, null, "localhost:6109", true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT TO http://localhost:6109 USER dev@Local.org IDENTIFIED BY 123456", null,
                testProcessor("http://localhost:6109", "dev@Local.org", "123456", "localhost:6109", true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT TO http://localhost:6109;", null,
                testProcessor("http://localhost:6109", null, null, "localhost:6109", true)));
        assertTrue(AdminQueryProcessor.processLoginCommand("CONNECT TO http://localhost:6109 USER dev@Local.org IDENTIFIED BY 123456;", null,
                testProcessor("http://localhost:6109", "dev@Local.org", "123456", "localhost:6109", true)));
    }

    private AdminQueryProcessor.LoginCommandProcessor testProcessor(String expectedUrl, String expectedUserName, String expectedUserPass, String expectedHost, boolean result) throws SQLException {
        return (url, userName, userPass, host) -> {
            assertEquals(expectedUrl, url);
            assertEquals(expectedUserName, userName);
            assertEquals(expectedUserPass, userPass);
            assertEquals(expectedHost, host);
            return result;
        };
    }
}
