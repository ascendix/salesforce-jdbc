package com.ascendix.jdbc.salesforce.statement.processor;

import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public class InsertQueryProcessor {

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
}
