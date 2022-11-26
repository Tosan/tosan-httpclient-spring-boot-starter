package com.tosan.client.http.resttemplate.starter.impl;

import com.tosan.client.http.core.HttpClientProperties;
import org.springframework.web.client.RestTemplate;

/**
 * @author Ali Alimohammadi
 * @since 8/6/2022
 */
public class ExternalServiceInvoker {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ExternalServiceInvoker(RestTemplate restTemplate, HttpClientProperties httpClientProperties) {
        this.restTemplate = restTemplate;
        baseUrl = httpClientProperties.getBaseServiceUrl();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String generateUrl(String url) {
        return baseUrl + url;
    }
}
