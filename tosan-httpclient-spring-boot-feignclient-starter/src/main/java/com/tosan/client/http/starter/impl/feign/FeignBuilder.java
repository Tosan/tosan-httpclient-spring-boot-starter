package com.tosan.client.http.starter.impl.feign;

import feign.Feign;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public class FeignBuilder {

    private final Feign.Builder feignBuilder;
    private final CloseableHttpClient httpClient;

    public FeignBuilder(Feign.Builder feignBuilder, CloseableHttpClient httpClient) {
        this.feignBuilder = feignBuilder;
        this.httpClient = httpClient;
    }

    public Feign.Builder getFeignBuilder() {
        return feignBuilder;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
}
