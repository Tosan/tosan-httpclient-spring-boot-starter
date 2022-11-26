package com.tosan.client.http.sample.server.api.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.sample.server.api.config.properties.CustomServerClientConfig;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.CustomServerException;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.starter.configuration.AbstractFeignConfiguration;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.ExceptionExtractType;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Configuration
@EnableFeignClients
@Slf4j
public class CustomServerFeignConfig extends AbstractFeignConfiguration {

    @Override
    @Bean("customServer-objectMapper")
    public ObjectMapper objectMapper() {
        return super.objectMapper();
    }

    @Bean("customServer-clientConfig")
    @ConfigurationProperties(prefix = "custom-service")
    @ConditionalOnMissingBean(name = "customServer-clientConfig")
    public HttpClientProperties customServerClientConfig() {
        return new CustomServerClientConfig();
    }

    @Override
    @Bean("customServer-apacheHttpClientFactory")
    public ApacheHttpClientFactory apacheHttpClientFactory(
            @Qualifier("customServer-httpClientBuilder") HttpClientBuilder builder,
            @Qualifier("customServer-connectionManagerFactory") ApacheHttpClientConnectionManagerFactory clientConnectionManagerFactory,
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig) {
        return super.apacheHttpClientFactory(builder, clientConnectionManagerFactory, customServerClientConfig);
    }

    @Override
    @Bean("customServer-clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Qualifier("customServer-apacheHttpClientFactory") ApacheHttpClientFactory apacheHttpClientFactory) {
        return super.clientHttpRequestFactory(apacheHttpClientFactory);
    }

    @Override
    @Bean("customServer-httpclient")
    public CloseableHttpClient httpClient(
            @Qualifier("customServer-apacheHttpClientFactory") ApacheHttpClientFactory apacheHttpClientFactory) {
        return super.httpClient(apacheHttpClientFactory);
    }

    @Override
    @Bean("customServer-connectionManagerFactory")
    public ApacheHttpClientConnectionManagerFactory connectionManagerFactory() {
        return super.connectionManagerFactory();
    }

    @Override
    @Bean("customServer-feignClient")
    public Client feignClient(@Qualifier("customServer-httpclient") HttpClient httpClient) {
        return super.feignClient(httpClient);
    }

    @Override
    @Bean("customServer-requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return super.requestInterceptor();
    }

    @Override
    @Bean("customServer-requestInterceptors")
    public List<RequestInterceptor> requestInterceptors(
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig,
            @Qualifier("customServer-requestInterceptor") RequestInterceptor requestInterceptor) {
        return super.requestInterceptors(customServerClientConfig, requestInterceptor);
    }

    @Override
    @Bean("customServer-feignContract")
    public Contract feignContract() {
        return super.feignContract();
    }

    @Override
    @Bean("customServer-feignEncoder")
    public Encoder feignEncoder(@Qualifier("customServer-jacksonHttpMessageConverter") HttpMessageConverter<Object> httpMessageConverter) {
        return super.feignEncoder(httpMessageConverter);
    }

    @Override
    @Bean("customServer-feignDecoder")
    public Decoder feignDecoder(@Qualifier("customServer-jacksonHttpMessageConverter") HttpMessageConverter<Object> httpMessageConverter) {
        return super.feignDecoder(httpMessageConverter);
    }

    @Override
    @Bean("customServer-jacksonHttpMessageConverter")
    public HttpMessageConverter<Object> httpMessageConverter(@Qualifier("customServer-objectMapper")
                                                                     ObjectMapper objectMapper) {
        return super.httpMessageConverter(objectMapper);
    }

    @Override
    @Bean("customServer-feignErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig(@Qualifier("customServer-objectMapper") ObjectMapper objectMapper) {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        customErrorDecoderConfig.getScanPackageList().add("com.tosan.client.http.sample.server.api.exception");
        customErrorDecoderConfig.setExceptionExtractType(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS);
        customErrorDecoderConfig.setCheckedExceptionClass(CustomServerException.class);
        customErrorDecoderConfig.setUncheckedExceptionClass(TosanWebServiceRuntimeException.class);
        customErrorDecoderConfig.setObjectMapper(objectMapper);
        return customErrorDecoderConfig;
    }

    @Override
    @Bean("customServer-feignErrorDecoder")
    public CustomErrorDecoder customErrorDecoder(@Qualifier("customServer-feignErrorDecoderConfig") CustomErrorDecoderConfig customErrorDecoderConfig) {
        return super.customErrorDecoder(customErrorDecoderConfig);
    }

    @Override
    @Bean("customServer-httpClientBuilder")
    public HttpClientBuilder apacheHttpClientBuilder() {
        return super.apacheHttpClientBuilder();
    }

    @Override
    @Bean("customServer-retryer")
    @ConditionalOnMissingBean(
            name = {"customServer-retryer"}
    )
    public Retryer retryer() {
        return super.retryer();
    }

    @Override
    @Bean("customServer-feignLoggerLevel")
    @ConditionalOnMissingBean(
            name = {"customServer-feignLoggerLevel"}
    )
    public Logger.Level feignLoggerLevel() {
        return super.feignLoggerLevel();
    }

    @Override
    @Bean("customServer-feignOption")
    public Request.Options options(
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig) {
        return super.options(customServerClientConfig);
    }

    @Override
    @Bean("customServer-feignBuilder")
    public Feign.Builder feignBuilder(@Qualifier("customServer-feignClient") Client feignClient,
                                      @Qualifier("customServer-feignOption") Request.Options options,
                                      @Qualifier("customServer-requestInterceptors") List<RequestInterceptor> requestInterceptors,
                                      @Qualifier("customServer-feignContract") Contract feignContract,
                                      @Qualifier("customServer-feignDecoder") Decoder feignDecoder,
                                      @Qualifier("customServer-feignEncoder") Encoder feignEncoder,
                                      @Qualifier("customServer-retryer") Retryer retryer,
                                      @Qualifier("customServer-feignLoggerLevel") Logger.Level logLevel,
                                      @Qualifier("customServer-feignErrorDecoder") CustomErrorDecoder customErrorDecoder) {

        return super.feignBuilder(feignClient, options, requestInterceptors, feignContract, feignDecoder, feignEncoder,
                retryer, logLevel, customErrorDecoder);
    }

    @Bean
    public CustomServerRestController clientServerRestController(
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig,
            @Qualifier("customServer-feignBuilder") Feign.Builder feignBuilder) {
        return feignBuilder
                .logger(new Slf4jLogger(CustomServerRestController.class))
                .target(CustomServerRestController.class, customServerClientConfig.getBaseServiceUrl()
                        + CustomServerRestController.PATH);
    }
}
