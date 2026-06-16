package com.tosan.client.http.restclient.starter.impl.interceptor;

import com.tosan.client.http.restclient.starter.exception.HttpClientRequestExecuteException;
import com.tosan.client.http.restclient.starter.impl.interceptor.wrapper.HttpResponseWrapper;
import com.tosan.client.http.restclient.starter.util.HttpLoggingInterceptorUtil;
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
        if (log.isInfoEnabled()) {
            log.info(httpLoggingInterceptorUtil.getRequestDetailContent(request, requestBody, webServiceName));
        }
        ClientHttpResponse response;
        long startTime = System.currentTimeMillis();
        try {
            response = ex.execute(request, requestBody);
            if (log.isInfoEnabled()) {
                HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response);
                log.info(httpLoggingInterceptorUtil.getResponseDetailContent(responseWrapper, webServiceName,
                        System.currentTimeMillis() - startTime));
                return responseWrapper;
            } else {
                return response;
            }
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info(httpLoggingInterceptorUtil.getExceptionDetailContent(e, webServiceName,
                        System.currentTimeMillis() - startTime));
            }
            throw new HttpClientRequestExecuteException(e.getMessage(), e);
        }
    }
}
