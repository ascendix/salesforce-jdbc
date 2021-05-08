package com.ascendix.jdbc.salesforce.exceptions;

public class UnsupportedArgumentTypeException extends RuntimeException {

    public UnsupportedArgumentTypeException(String message) {
        super(message);
    }

    public UnsupportedArgumentTypeException(String message, Throwable cause) {
        super(message, cause);
    }

}
