package com.tosan.client.http.core.exception;


public class InfrastructureFailureException extends RuntimeException {

    public InfrastructureFailureException(String message) {
        super(message);
    }

    public InfrastructureFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
