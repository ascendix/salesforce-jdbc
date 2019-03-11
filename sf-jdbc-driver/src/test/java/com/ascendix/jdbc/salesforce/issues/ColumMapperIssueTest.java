package com.ascendix.jdbc.salesforce.issues;

import com.ascendix.jdbc.salesforce.ForceDriver;
import com.ascendix.jdbc.salesforce.connection.ForceConnection;
import com.ascendix.jdbc.salesforce.delegates.ForceResultField;
import com.ascendix.jdbc.salesforce.metadata.ForceDatabaseMetaData;
import com.ascendix.jdbc.salesforce.resultset.CachedResultSet;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ColumMapperIssueTest {

    private final String QUERY = "CACHE GLOBAL select  ascendix__Property__c.ascendix__ListingBrokerCompany__r.Website," +
            " ascendix__Property__c.ascendix__ListingBrokerCompany__r.Name, " +
            " ascendix__Property__c.Name" +
            " from ascendix__Property__c " +
            "where ascendix__Property__c.Id ='a0Jf4000001eSpZEAU'";

    private final String QUERY1 ="select Account.Name, Account.Id from Account where Account.Id = '001f400000MRn1yAAD'";

    private ForceDriver driver;


    @Before
    public void setUp() throws Exception {
        driver = new ForceDriver();
    }

    @Test
    @Ignore
    public void indexOutOfBoundsIssue() {
        Properties properties = new Properties();
        String sessionId = "00D0R0000000XOG!AQMAQMgO5r33Ym_8v8xpPN5UNfXphvBPXC01I4dY4.6fBsYqNoAJbdwOhQaQSbcdPNyndJzJKUVc_6qqWh.1rJakw1t3jgcQ";
        properties.put("sessionId", sessionId);
        try {
            ForceConnection connection = (ForceConnection) driver.connect("jdbc:ascendix:salesforce", properties);
            ForcePreparedStatement statement = new ForcePreparedStatement(connection, QUERY);
            CachedResultSet cachedResultSet = (CachedResultSet)statement.executeQuery();
            while(cachedResultSet.next()){
                System.out.println(cachedResultSet.getString(1));
                //System.out.println(cachedResultSet.getString(2));
                System.out.println("OK");
            }

            System.out.println("OK");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
