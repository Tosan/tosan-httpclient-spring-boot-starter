package com.tosan.client.http.starter.impl.feign.exception;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class FeignConfigurationException extends RuntimeException {

    public FeignConfigurationException(String message) {
        super(message);
    }

    public FeignConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
