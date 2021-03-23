package com.ascendix.jdbc.salesforce.statement.processor;

import com.google.common.collect.Sets;
import com.sforce.soap.partner.DescribeSObjectResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class InsertQueryAnalyzerTest {

    @Test
    public void testIsInsertQuery() {
        String soql = "insert into Account(Name, OwnerId) values ('FirstAccount', '005xx1231231233123')";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        InsertQueryAnalyzer analyzer = new InsertQueryAnalyzer(soql, this::describeSObject, cache, null);

        assertTrue(analyzer.analyse(soql));
    }

    private DescribeSObjectResult describeSObject(String objName) {
        DescribeSObjectResult result = new DescribeSObjectResult();
        result.setName(objName);
        return result;
    }

    @Test
    public void testProcessInsert_ValuesOne() {
        String soql = "insert into Account(Name, OwnerId, Title) values ('FirstAccount', '005xx1231231233123', Null)";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        InsertQueryAnalyzer analyzer = new InsertQueryAnalyzer(soql, this::describeSObject, cache, null);

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly one record to save
        assertEquals(1, analyzer.getRecords().size());
        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(3, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "OwnerId", "Title"), record.keySet());
        assertEquals("FirstAccount", record.get("Name"));
        assertEquals("005xx1231231233123", record.get("OwnerId"));
        assertTrue(record.containsKey("Title"));
        assertNull(record.get("Title"));
    }

    @Test
    public void testProcessInsert_ValuesOneSubSelect() {
        String soql = "insert into Account(Name, OwnerId) values ('FirstAccount', " +
                " (SELECT Id from User where Name='CollectionOwner-f CollectionOwner-l' LIMIT 1) " +
                ")";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        InsertQueryAnalyzer analyzer = new InsertQueryAnalyzer(soql, this::describeSObject, cache, subSoql -> {
            if ("SELECT Id FROM User WHERE Name = 'CollectionOwner-f CollectionOwner-l' LIMIT 1".equals(subSoql)) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", "005xx1231231233123");
                return Arrays.asList(record);
            }
            return Arrays.asList();
        });

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly one record to save
        assertEquals(1, analyzer.getRecords().size());
        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "OwnerId"), record.keySet());
        assertEquals("FirstAccount", record.get("Name"));
        assertEquals("005xx1231231233123", record.get("OwnerId"));
    }

    @Test
    public void testProcessInsert_ValuesTwo() {
        String soql = "insert into Account(Name, OwnerId) values ('FirstAccount', '005xx1111111111111'),  ('SecondAccount', '005xx2222222222222')";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        InsertQueryAnalyzer analyzer = new InsertQueryAnalyzer(soql, this::describeSObject, cache, null);

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly one record to save
        assertEquals(2, analyzer.getRecords().size());

        // Verify the first record
        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "OwnerId"), record.keySet());
        assertEquals("FirstAccount", record.get("Name"));
        assertEquals("005xx1111111111111", record.get("OwnerId"));

        // Verify the second record
        record = analyzer.getRecords().get(1);
        // Verify the fields count for the second record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "OwnerId"), record.keySet());
        assertEquals("SecondAccount", record.get("Name"));
        assertEquals("005xx2222222222222", record.get("OwnerId"));
    }
}
