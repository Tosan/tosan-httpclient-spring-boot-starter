package com.tosan.client.http.sample.server.api.config;

import com.tosan.client.http.starter.impl.feign.exception.FeignConfigurationException;

import java.io.Serial;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
public class CustomServerClientConfigurationException extends FeignConfigurationException {
    @Serial
    private static final long serialVersionUID = -2443361687977670214L;

    public CustomServerClientConfigurationException(String message) {
        super(message);
    }

    public CustomServerClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
