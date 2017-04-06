package com.ascendix.jdbc.salesforce;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ForceDriverTest {

	private ForceDriver driver;
	
	@Before
	public void setUp() throws Exception {
		driver = new ForceDriver();
	}

	@Test
	public void testGetUrlProperty_WhenFirst() {
		String actual = driver.getUrlProperty("protocol://myProp = value", "myProp");
		assertEquals(" value", actual);
	}

	@Test
	public void testGetUrlProperty_WhenFirstWithoutSlashes() {
		String actual = driver.getUrlProperty("protocol:myProp = value", "myProp");
		assertEquals(" value", actual);
	}

	@Test
	public void testGetUrlProperty_WhenNotFirst() {
		String actual = driver.getUrlProperty("protocol://myProp2 = value2; myProp=value;", "myProp");
		assertEquals("value", actual);
	}

	@Test
	public void testGetUrlProperty_IfNotExists() {
		String actual = driver.getUrlProperty("protocol://domain.com;myProp = value", "myProp2");
		assertNull(actual);
	}

}
