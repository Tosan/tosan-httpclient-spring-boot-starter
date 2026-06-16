package com.tosan.client.http.restclient.starter.impl;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class ClientService {
    private final RestClient restClient;
    private final HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;

    public ClientService(RestClient restClient, HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory) {
        this.restClient = restClient;
        this.httpComponentsClientHttpRequestFactory = httpComponentsClientHttpRequestFactory;
    }

    public HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory() {
        return httpComponentsClientHttpRequestFactory;
    }

    public RestClient getRestClient() {
        return restClient;
    }
}
