package com.tosan.client.http.starter.impl.feign.exception;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
public class FeignClientRequestExecuteException extends RuntimeException {

    public FeignClientRequestExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
