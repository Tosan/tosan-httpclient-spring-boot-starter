package com.tosan.client.http.core.exception;


public class ExternalServiceCircuitBreakerException extends RuntimeException {

    public ExternalServiceCircuitBreakerException(String message) {
        super(message);
    }

    public ExternalServiceCircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }
}
