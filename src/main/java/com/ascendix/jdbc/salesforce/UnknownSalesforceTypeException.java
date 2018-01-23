package com.ascendix.jdbc.salesforce;

import java.sql.SQLException;

public class UnknownSalesforceTypeException extends SQLException {

    private static final long serialVersionUID = -2917019809039695984L;

    public UnknownSalesforceTypeException(String type) {
	super("Unknow Salesforce type found: " + type);
    }
    
    

}
