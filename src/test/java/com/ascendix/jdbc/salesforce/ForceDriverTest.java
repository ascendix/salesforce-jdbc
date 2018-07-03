package com.ascendix.jdbc.salesforce;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ForceDriverTest {

    private ForceDriver driver;

    @Before
    public void setUp() throws Exception {
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

}
