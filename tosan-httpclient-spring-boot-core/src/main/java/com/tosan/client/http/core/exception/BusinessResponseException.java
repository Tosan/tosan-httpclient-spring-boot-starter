package com.tosan.client.http.core.exception;


public class BusinessResponseException extends RuntimeException implements CircuitBreakerBusinessException {

    public BusinessResponseException(String message) {
        super(message);
    }

    public BusinessResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
