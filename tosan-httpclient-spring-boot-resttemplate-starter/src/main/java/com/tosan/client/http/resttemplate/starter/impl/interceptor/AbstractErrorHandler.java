package com.tosan.client.http.resttemplate.starter.impl.interceptor;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * @author Ali Alimohammadi
 * @since 8/16/2022
 */
public abstract class AbstractErrorHandler extends DefaultResponseErrorHandler {

    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        } catch (Exception exception) {
            mappingException(exception);
        }
    }

    public abstract void mappingException(Exception exception);
}
