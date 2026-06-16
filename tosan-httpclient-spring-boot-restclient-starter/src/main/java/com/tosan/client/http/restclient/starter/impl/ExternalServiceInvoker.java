package com.tosan.client.http.restclient.starter.impl;

import com.tosan.client.http.core.HttpClientProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * @author Ali Alimohammadi
 * @since 8/6/2022
 */
public class ExternalServiceInvoker implements DisposableBean {
    private final HttpClientProperties httpClientProperties;
    private final ClientService clientService;

    public ExternalServiceInvoker(ClientService clientService, HttpClientProperties httpClientProperties) {
        this.clientService = clientService;
        this.httpClientProperties = httpClientProperties;
    }

    public RestClient getClient() {
        return this.clientService.getRestClient();
    }

    public String generateUrl(String path) {
        String baseUrl = this.httpClientProperties.getBaseServiceUrl();
        if (path == null || path.isBlank()) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    @Override
    public void destroy() throws Exception {
        clientService.getHttpComponentsClientHttpRequestFactory().destroy();
    }
}
