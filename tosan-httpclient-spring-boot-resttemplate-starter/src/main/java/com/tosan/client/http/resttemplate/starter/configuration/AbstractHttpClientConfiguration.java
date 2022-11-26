package com.tosan.client.http.resttemplate.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.factory.ConfigurableApacheHttpClientFactory;
import com.tosan.client.http.resttemplate.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.resttemplate.starter.impl.interceptor.HttpLoggingInterceptor;
import com.tosan.client.http.resttemplate.starter.util.HttpLoggingInterceptorUtil;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public abstract class AbstractHttpClientConfiguration {

    public abstract String getExternalServiceName();

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    public abstract HttpClientProperties clientConfig();

    public ApacheHttpClientFactory apacheHttpClientFactory(
            HttpClientBuilder builder,
            ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
            HttpClientProperties httpClientProperties) {
        return new ConfigurableApacheHttpClientFactory(builder, connectionManagerFactory, httpClientProperties);
    }

    public ClientHttpRequestFactory clientHttpRequestFactory(ApacheHttpClientFactory apacheHttpClientFactory) {
        return new HttpComponentsClientHttpRequestFactory(apacheHttpClientFactory.createBuilder().build());
    }

    public HttpClientBuilder apacheHttpClientBuilder() {
        return HttpClientBuilder.create();
    }

    public ApacheHttpClientConnectionManagerFactory connectionManagerFactory() {
        return new DefaultApacheHttpClientConnectionManagerFactory();
    }

    public HttpMessageConverter<Object> httpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return converter;
    }

    public RestTemplateBuilder restTemplateBuilder(
            HttpMessageConverter<Object> httpMessageConverter,
            ClientHttpRequestFactory clientHttpRequestFactory,
            List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors,
            ResponseErrorHandler responseErrorHandler) {
        return new RestTemplateBuilder()
                .messageConverters(httpMessageConverter)
                .requestFactory(() -> clientHttpRequestFactory)
                .additionalInterceptors(clientHttpRequestInterceptors)
                .errorHandler(responseErrorHandler);
    }

    public ClientHttpRequestInterceptor httpLoggingRequestInterceptor(HttpLoggingInterceptorUtil httpLoggingInterceptorUtil) {
        return new HttpLoggingInterceptor(httpLoggingInterceptorUtil, getExternalServiceName());
    }

    public List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors(
            HttpClientProperties httpClientProperties,
            ClientHttpRequestInterceptor httpLoggingRequestInterceptor) {
        List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors = new ArrayList<>();
        clientHttpRequestInterceptors.add(httpLoggingRequestInterceptor);
        HttpClientProperties.AuthorizationConfiguration authorizationConfiguration =
                httpClientProperties.getAuthorization();
        if (httpClientProperties.getAuthorization().isEnable()) {
            clientHttpRequestInterceptors.add(new BasicAuthenticationInterceptor(authorizationConfiguration.getUsername(),
                    authorizationConfiguration.getPassword(), StandardCharsets.UTF_8));
        }
        return clientHttpRequestInterceptors;
    }

    public abstract ResponseErrorHandler responseErrorHandler();

    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public ExternalServiceInvoker serviceInvoker(RestTemplate restTemplate, HttpClientProperties httpClientProperties) {
        return new ExternalServiceInvoker(restTemplate, httpClientProperties);
    }
}
