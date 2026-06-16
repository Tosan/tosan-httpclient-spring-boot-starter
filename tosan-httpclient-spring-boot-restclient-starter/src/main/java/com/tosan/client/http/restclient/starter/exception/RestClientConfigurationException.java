package com.tosan.client.http.restclient.starter.exception;

public class RestClientConfigurationException extends RuntimeException {

    public RestClientConfigurationException(String message) {
        super(message);
    }

    public RestClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}