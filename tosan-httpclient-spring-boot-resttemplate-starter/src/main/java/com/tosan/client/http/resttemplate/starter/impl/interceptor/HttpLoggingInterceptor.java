package com.tosan.client.http.resttemplate.starter.impl.interceptor;

import com.tosan.client.http.resttemplate.starter.exception.HttpClientRequestExecuteException;
import com.tosan.client.http.resttemplate.starter.impl.interceptor.wrapper.HttpResponseWrapper;
import com.tosan.client.http.resttemplate.starter.util.HttpLoggingInterceptorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
    private final HttpLoggingInterceptorUtil httpLoggingInterceptorUtil;
    private final String webServiceName;

    public HttpLoggingInterceptor(HttpLoggingInterceptorUtil httpLoggingInterceptorUtil, String webServiceName) {
        this.httpLoggingInterceptorUtil = httpLoggingInterceptorUtil;
        this.webServiceName = webServiceName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody, ClientHttpRequestExecution ex) {
        log.info(httpLoggingInterceptorUtil.getRequestDetailContent(request, requestBody, webServiceName));
        ClientHttpResponse response;
        HttpResponseWrapper responseWrapper;
        try {
            response = ex.execute(request, requestBody);
            responseWrapper = new HttpResponseWrapper(response);
            log.info(httpLoggingInterceptorUtil.getResponseDetailContent(responseWrapper, webServiceName));
            return responseWrapper;
        } catch (IOException e) {
            log.info(httpLoggingInterceptorUtil.getExceptionDetailContent(e, webServiceName));
            throw new HttpClientRequestExecuteException(e.getMessage(), e);
        }
    }
}