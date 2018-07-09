package com.ascendix.jdbc.salesforce.oauth;

public class BadOAuthTokenException extends RuntimeException {

    public BadOAuthTokenException(String message) {
        super(message);
    }
}
