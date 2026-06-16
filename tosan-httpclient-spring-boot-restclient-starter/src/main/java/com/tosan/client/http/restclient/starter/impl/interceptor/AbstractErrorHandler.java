package com.tosan.client.http.restclient.starter.impl.interceptor;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

/**
 * @author Ali Alimohammadi
 * @since 8/16/2022
 */
public abstract class AbstractErrorHandler extends DefaultResponseErrorHandler {

    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        try {
            super.handleError(url, method, response);
        } catch (Exception exception) {
            this.mappingException(exception);
        }
    }

    public abstract void mappingException(Exception exception);
}
