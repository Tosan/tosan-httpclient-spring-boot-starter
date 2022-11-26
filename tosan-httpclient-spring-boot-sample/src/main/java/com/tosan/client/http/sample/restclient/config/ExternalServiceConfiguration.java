package com.tosan.client.http.sample.restclient.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.sample.restclient.exception.ExceptionHandler;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.resttemplate.starter.configuration.AbstractHttpClientConfiguration;
import com.tosan.client.http.resttemplate.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.resttemplate.starter.util.HttpLoggingInterceptorUtil;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Ali Alimohammadi
 * @since 8/6/2022
 */
@Configuration
public class ExternalServiceConfiguration extends AbstractHttpClientConfiguration {

    @Override
    public String getExternalServiceName() {
        return "custom-web-service";
    }

    @Bean("external-objectMapper")
    @Override
    public ObjectMapper objectMapper() {
        return super.objectMapper();
    }

    @Bean("external-clientConfig")
    @ConfigurationProperties(prefix = "custom-web-service.client")
    @Override
    public HttpClientProperties clientConfig() {
        return new HttpClientProperties();
    }

    @Bean("external-apacheHttpClientFactory")
    @Override
    public ApacheHttpClientFactory apacheHttpClientFactory(
            @Qualifier("external-apacheHttpClientBuilder") HttpClientBuilder builder,
            @Qualifier("external-connectionManagerFactory") ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
            @Qualifier("external-clientConfig") HttpClientProperties httpClientProperties) {
        return super.apacheHttpClientFactory(builder, connectionManagerFactory, httpClientProperties);
    }

    @Bean("external-clientHttpRequestFactory")
    @Override
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Qualifier("external-apacheHttpClientFactory") ApacheHttpClientFactory apacheHttpClientFactory) {
        return super.clientHttpRequestFactory(apacheHttpClientFactory);
    }

    @Bean("external-apacheHttpClientBuilder")
    @Override
    public HttpClientBuilder apacheHttpClientBuilder() {
        return super.apacheHttpClientBuilder();
    }

    @Bean("external-connectionManagerFactory")
    @Override
    public ApacheHttpClientConnectionManagerFactory connectionManagerFactory() {
        return super.connectionManagerFactory();
    }

    @Bean("external-httpMessageConverter")
    @Override
    public HttpMessageConverter<Object> httpMessageConverter(
            @Qualifier("external-objectMapper") ObjectMapper objectMapper) {
        return super.httpMessageConverter(objectMapper);
    }

    @Bean("external-restTemplateBuilder")
    @Override
    public RestTemplateBuilder restTemplateBuilder(
            @Qualifier("external-httpMessageConverter") HttpMessageConverter<Object> httpMessageConverter,
            @Qualifier("external-clientHttpRequestFactory") ClientHttpRequestFactory clientHttpRequestFactory,
            @Qualifier("external-clientHttpRequestInterceptors") List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors,
            @Qualifier("external-responseErrorHandler") ResponseErrorHandler responseErrorHandler) {
        return super.restTemplateBuilder(httpMessageConverter, clientHttpRequestFactory, clientHttpRequestInterceptors,
                responseErrorHandler);
    }

    @Bean("external-httpLoggingRequestInterceptor")
    @Override
    public ClientHttpRequestInterceptor httpLoggingRequestInterceptor(HttpLoggingInterceptorUtil httpLoggingInterceptorUtil) {
        return super.httpLoggingRequestInterceptor(httpLoggingInterceptorUtil);
    }

    @Bean("external-clientHttpRequestInterceptors")
    @Override
    public List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors(
            @Qualifier("external-clientConfig") HttpClientProperties httpClientProperties,
            @Qualifier("external-httpLoggingRequestInterceptor") ClientHttpRequestInterceptor httpLoggingRequestInterceptor) {
        return super.clientHttpRequestInterceptors(httpClientProperties, httpLoggingRequestInterceptor);
    }

    @Bean("external-responseErrorHandler")
    @Override
    public ResponseErrorHandler responseErrorHandler() {
        return new ExceptionHandler();
    }

    @Bean("external-restTemplate")
    @Override
    public RestTemplate restTemplate(@Qualifier("external-restTemplateBuilder") RestTemplateBuilder builder) {
        return super.restTemplate(builder);
    }

    @Bean("external-serviceInvoker")
    @Override
    public ExternalServiceInvoker serviceInvoker(
            @Qualifier("external-restTemplate") RestTemplate restTemplate,
            @Qualifier("external-clientConfig") HttpClientProperties httpClientProperties) {
        return super.serviceInvoker(restTemplate, httpClientProperties);
    }
}
