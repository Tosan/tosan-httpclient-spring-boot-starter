package com.tosan.client.http.restclient.starter.impl;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.service.ExternalService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.client.RestClient;

/**
 * @author Ali Alimohammadi
 * @since 8/6/2022
 */
public class ExternalServiceInvoker<P extends HttpClientProperties>
        implements ExternalService<RestClient, P>, DisposableBean {

    private final String serviceName;
    private final P httpClientProperties;
    private final ClientService clientService;

    public ExternalServiceInvoker(String serviceName, ClientService clientService, P httpClientProperties) {
        this.serviceName = serviceName;
        this.clientService = clientService;
        this.httpClientProperties = httpClientProperties;
    }

    @Override
    public P getProperties() {
        return this.httpClientProperties;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
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
