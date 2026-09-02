package com.tosan.client.http.starter.impl.feign;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.service.ExternalService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.DisposableBean;

public class ExternalServiceInvoker<T, P extends HttpClientProperties>
        implements ExternalService<T, P>, DisposableBean {

    private final String serviceName;
    private final T client;
    private final P properties;
    private final CloseableHttpClient httpClient;

    public ExternalServiceInvoker(String serviceName, T client, P properties,
                                  CloseableHttpClient httpClient) {

        this.serviceName = serviceName;
        this.client = client;
        this.properties = properties;
        this.httpClient = httpClient;
    }

    @Override
    public P getProperties() {
        return properties;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public T getClient() {
        return client;
    }

    @Override
    public void destroy() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
