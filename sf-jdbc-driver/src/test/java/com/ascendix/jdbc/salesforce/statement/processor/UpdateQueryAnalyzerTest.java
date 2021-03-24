package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.statement.processor.utils.RecordFieldsBuilder;
import com.google.common.collect.Sets;
import com.sforce.soap.partner.DescribeSObjectResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UpdateQueryAnalyzerTest {

    @Test
    public void testIsUpdateQuery_ById() {
        String soql = "Update Account set Name ='FirstAccount_new' where Id='001xx000003GeY0AAK'";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        UpdateQueryAnalyzer analyzer = new UpdateQueryAnalyzer(soql, this::describeSObject, cache, null);

        assertTrue(analyzer.analyse(soql));
    }

    private DescribeSObjectResult describeSObject(String objName) {
        DescribeSObjectResult result = new DescribeSObjectResult();
        result.setName(objName);
        return result;
    }

    @Test
    public void testProcessUpdate_One_ById() {
        String soql = "Update Account set Name ='FirstAccount_new' where Id='001xx000003GeY0AAK'";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        UpdateQueryAnalyzer analyzer = new UpdateQueryAnalyzer(soql, this::describeSObject, cache, null);

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly one record to save
        assertEquals(1, analyzer.getRecords().size());
        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("FirstAccount_new", record.get("Name"));
        assertEquals("001xx000003GeY0AAK", record.get("Id"));
    }

    @Test
    public void testProcessUpdate_One_ByName() {
        String soql = "Update Account set Name ='NEW_AccountName' where Name='FirstAccount_new'";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        UpdateQueryAnalyzer analyzer = new UpdateQueryAnalyzer(soql, this::describeSObject, cache, subSoql -> {
            if ("SELECT Id FROM Account WHERE Name = 'FirstAccount_new'".equals(subSoql)) {
                return Arrays.asList(
                        RecordFieldsBuilder.id("005xx1111111111111"),
                        RecordFieldsBuilder.id("005xx2222222222222"),
                        RecordFieldsBuilder.id("005xx3333333333333")
                       );
            }
            return Arrays.asList();
        });

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly three record to save
        assertEquals(3, analyzer.getRecords().size());

        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx1111111111111", record.get("Id"));

        record = analyzer.getRecords().get(1);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx2222222222222", record.get("Id"));

        record = analyzer.getRecords().get(2);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx3333333333333", record.get("Id"));
    }

    @Test
    public void testProcessUpdate_One_ByName_CALC() {
        String soql = "Update Account set Name=Name+'-' where Name='FirstAccount_new'";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        UpdateQueryAnalyzer analyzer = new UpdateQueryAnalyzer(soql, this::describeSObject, cache, subSoql -> {
            if ("SELECT Id, Name FROM Account WHERE Name = 'FirstAccount_new'".equals(subSoql)) {
                return Arrays.asList(
                        RecordFieldsBuilder.setId("005xx1111111111111").set("Name", "Acc_01").build(),
                        RecordFieldsBuilder.setId("005xx2222222222222").set("Name", "Acc_02").build(),
                        RecordFieldsBuilder.setId("005xx3333333333333").set("Name", "Acc_03").build()
                       );
            }
            return Arrays.asList();
        });

        assertTrue(analyzer.analyse(soql));
        assertEquals("Account", analyzer.getFromObjectName());

        // Verify we have exactly three record to save
        assertEquals(3, analyzer.getRecords().size());

        Map<String, Object> record = analyzer.getRecords().get(0);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx1111111111111", record.get("Id"));

        record = analyzer.getRecords().get(1);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx2222222222222", record.get("Id"));

        record = analyzer.getRecords().get(2);
        // Verify the fields count for the first record
        assertEquals(2, record.size());
        // Verify the fields' names for the first record
        assertEquals(Sets.newHashSet("Name", "Id"), record.keySet());
        assertEquals("NEW_AccountName", record.get("Name"));
        assertEquals("005xx3333333333333", record.get("Id"));
    }

}
