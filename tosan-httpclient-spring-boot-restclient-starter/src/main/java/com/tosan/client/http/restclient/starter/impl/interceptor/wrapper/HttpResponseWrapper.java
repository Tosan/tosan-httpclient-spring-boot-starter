package com.tosan.client.http.restclient.starter.impl.interceptor.wrapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public class HttpResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse response;
    private byte[] body;

    public HttpResponseWrapper(ClientHttpResponse response) {
        this.response = response;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return this.response.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.response.getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.response.getHeaders();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.body == null) {
            this.body = StreamUtils.copyToByteArray(this.response.getBody());
        }
        return new ByteArrayInputStream(this.body);
    }

    @Override
    public void close() {
        this.response.close();
    }
}
