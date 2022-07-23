package com.tosan.client.http.starter.impl.feign.exception;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class JsonConvertException extends RuntimeException {

    public JsonConvertException(String message) {
        super(message);
    }

    public JsonConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
