package com.ascendix.jdbc.salesforce.statement.processor;

import com.sforce.soap.partner.DescribeSObjectResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

public class InsertQueryAnalyzerTest {

    @Test
    public void testIsInsertQuery() {
        String soql = "insert into Account(Name, OwnerId) values ('FirstAccount', '005xx1231231233123')";
        Map<String, DescribeSObjectResult> cache = new HashMap<>();
        InsertQueryAnalyzer analyzer = new InsertQueryAnalyzer(soql, prepareDescriber(), cache);

        assertTrue(analyzer.analyse(soql));
    }

    private Function<String, DescribeSObjectResult> prepareDescriber() {
        return (objName) -> describeSObject(objName);
    }

    private DescribeSObjectResult describeSObject(String objName) {
        DescribeSObjectResult result = new DescribeSObjectResult();
        result.setName(objName);
        return result;
    }
}
