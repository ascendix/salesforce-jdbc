package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.delegates.PartnerService;
import com.ascendix.jdbc.salesforce.resultset.CommandLogCachedResultSet;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

import java.sql.ResultSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public class InsertQueryProcessor {

    private static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    public String createSample(PartnerConnection partnerConnection) {
        String result = null;
        try {
            // Create a new sObject of type Contact
            // and fill out its fields.
            SObject contact = new SObject();
            contact.setType("Contact");
            contact.setField("FirstName", "Otto");
            contact.setField("LastName", "Jespersen");
            contact.setField("Salutation", "Professor");
            contact.setField("Phone", "(999) 555-1234");
            contact.setField("Title", "Philologist");

            // Add this sObject to an array
            SObject[] contacts = new SObject[1];
            contacts[0] = contact;
            // Make a create call and pass it the array of sObjects
            SaveResult[] results = partnerConnection.create(contacts);

            // Iterate through the results list
            // and write the ID of the new sObject
            // or the errors if the object creation failed.
            // In this case, we only have one result
            // since we created one contact.
            for (int j = 0; j < results.length; j++) {
                if (results[j].isSuccess()) {
                    result = results[j].getId();
                    System.out.println(
                            "\nA contact was created with an ID of: " + result
                    );
                } else {
                    // There were errors during the create call,
                    // go through the errors array and write
                    // them to the console
                    for (int i = 0; i < results[j].getErrors().length; i++) {
                        Error err = results[j].getErrors()[i];
                        System.out.println("Errors were found on item " + j);
                        System.out.println("Error code: " +
                                err.getStatusCode().toString());
                        System.out.println("Error message: " + err.getMessage());
                    }
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
        return result;
    }

    public static boolean isInsertQuery(String soqlQuery, InsertQueryAnalyzer queryAnalyzer) {
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            return false;
        }
        soqlQuery = soqlQuery.trim();

        return queryAnalyzer.analyse(soqlQuery);
    }



    public static ResultSet processQuery(ForcePreparedStatement statement, String soqlQuery, PartnerService partnerService) {
        CommandLogCachedResultSet resultSet = new CommandLogCachedResultSet();
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            resultSet.log("No SOQL or ADMIN query found");
            return resultSet;
        }

        return resultSet;
    }

}
