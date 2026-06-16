package com.tosan.client.http.starter.impl.feign;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.DisposableBean;

public class ExternalServiceInvoker<T> implements DisposableBean {
    private final T client;
    private final CloseableHttpClient httpClient;

    public ExternalServiceInvoker(T client, CloseableHttpClient httpClient) {
        this.client = client;
        this.httpClient = httpClient;
    }

    public T getClient() {
        return this.client;
    }

    @Override
    public void destroy() throws Exception {
        if (this.httpClient != null) {
            this.httpClient.close();
        }
    }
}
