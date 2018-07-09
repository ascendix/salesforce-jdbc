package com.ascendix.jdbc.salesforce.oauth;

public class BadOAuthTokenException extends Exception {

    public BadOAuthTokenException(String message) {
        super(message);
    }
}
