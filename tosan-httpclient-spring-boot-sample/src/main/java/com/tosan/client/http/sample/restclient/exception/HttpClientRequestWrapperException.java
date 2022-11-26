package com.tosan.client.http.sample.restclient.exception;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
public class HttpClientRequestWrapperException extends RuntimeException {
    private static final long serialVersionUID = -4538321135384662912L;

    public HttpClientRequestWrapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
