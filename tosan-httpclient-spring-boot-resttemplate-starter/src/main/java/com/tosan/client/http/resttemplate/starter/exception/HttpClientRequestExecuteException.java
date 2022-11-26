package com.tosan.client.http.resttemplate.starter.exception;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
public class HttpClientRequestExecuteException extends RuntimeException {
    private static final long serialVersionUID = -4282942249721252088L;

    public HttpClientRequestExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
