package com.tosan.client.http.core.service;

import com.tosan.client.http.core.HttpClientProperties;

public interface ExternalService<T, P extends HttpClientProperties> {

    P getProperties();

    String getServiceName();

    T getClient();
}
