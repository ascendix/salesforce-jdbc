package com.ascendix.jdbc.salesforce.oauth;

public class ForceClientException extends RuntimeException {

    public ForceClientException(String message) {
        super(message);
    }

    public ForceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
