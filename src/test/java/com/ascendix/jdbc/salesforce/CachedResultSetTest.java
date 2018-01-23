package com.ascendix.jdbc.salesforce;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class CachedResultSetTest {

    private CachedResultSet cachedResultSet;
    
    @Before
    public void setUp() throws Exception {
	ColumnMap<String, Object> columnMap = new ColumnMap<>();
	cachedResultSet = new CachedResultSet(columnMap);
    }

    @Test
    public void testParseDate() {
	Date actual = cachedResultSet.parseDate("2017-06-23");
	
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(actual);
	
	assertEquals(2017, calendar.get(Calendar.YEAR));
	assertEquals(Calendar.JUNE, calendar.get(Calendar.MONTH));
	assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
    }

}
