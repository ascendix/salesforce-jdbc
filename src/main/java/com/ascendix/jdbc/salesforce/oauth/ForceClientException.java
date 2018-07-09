package com.ascendix.jdbc.salesforce.oauth;

public class ForceClientException extends Exception {

    public ForceClientException(String message) {
        super(message);
    }

    public ForceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
