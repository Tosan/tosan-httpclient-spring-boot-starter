package com.tosan.client.http.sample.server.api.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.factory.ConfigurableApacheHttpClientFactory;
import com.tosan.client.http.sample.server.api.config.properties.CustomServerClientConfig;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.CustomServerException;
import com.tosan.client.http.starter.configuration.AbstractFeignConfiguration;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.ExceptionExtractType;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import com.tosan.tools.mask.starter.replace.JacksonReplaceHelper;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import com.tosan.tools.mask.starter.replace.RegexReplaceHelper;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.micrometer.MicrometerObservationCapability;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
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

    @Bean("customServer-replace-helper")
    public JsonReplaceHelperDecider replaceHelperDecider(
            JacksonReplaceHelper jacksonReplaceHelper, RegexReplaceHelper regexReplaceHelper,
            @Qualifier("customServer-secured-parameters") SecureParametersConfig secureParametersConfig) {
        return super.replaceHelperDecider(jacksonReplaceHelper, regexReplaceHelper, secureParametersConfig);
    }

    @Bean("customServer-httpFeignClientLogger")
    public Logger httpFeignClientLogger(
            @Qualifier("customServer-replace-helper") JsonReplaceHelperDecider replaceHelperDecider) {
        return super.httpFeignClientLogger(replaceHelperDecider, "custom-server");
    }

    @Bean("customServer-secured-parameters")
    @ConditionalOnMissingBean(name = "customServer-secured-parameters")
    public SecureParametersConfig secureParametersConfig() {
        return super.secureParametersConfig();
    }

    @Bean("customServer-clientConfig")
    @ConfigurationProperties(prefix = "custom-service")
    @ConditionalOnMissingBean(name = "customServer-clientConfig")
    public HttpClientProperties customServerClientConfig() {
        return new CustomServerClientConfig();
    }

    @Override
    @Bean("customServer-apacheHttpClientFactory")
    public ConfigurableApacheHttpClientFactory apacheHttpClientFactory(
            @Qualifier("customServer-httpClientBuilder") HttpClientBuilder builder,
            @Qualifier("customServer-connectionManagerFactory") PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder,
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig) {
        return super.apacheHttpClientFactory(builder, connectionManagerBuilder, customServerClientConfig);
    }

    @Override
    @Bean("customServer-clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Qualifier("customServer-apacheHttpClientFactory") ConfigurableApacheHttpClientFactory apacheHttpClientFactory) {
        return super.clientHttpRequestFactory(apacheHttpClientFactory);
    }

    @Override
    @Bean("customServer-httpclient")
    public CloseableHttpClient httpClient(
            @Qualifier("customServer-apacheHttpClientFactory") ConfigurableApacheHttpClientFactory apacheHttpClientFactory) {
        return super.httpClient(apacheHttpClientFactory);
    }

    @Override
    @Bean("customServer-connectionManagerFactory")
    public PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder() {
        return super.connectionManagerBuilder();
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
    public Contract feignContractWithCustomSpringConversion(
            ConversionService feignConversionService, List<AnnotatedParameterProcessor> processors) {
        return super.feignContractWithCustomSpringConversion(feignConversionService, processors);
    }

    @Override
    @Bean("customServer-feignConversionService")
    public FormattingConversionService feignConversionService(List<FeignFormatterRegistrar> feignFormatterRegistrars) {
        return super.feignConversionService(feignFormatterRegistrars);
    }


    @Override
    @Bean("customServer-feignEncoder")
    public Encoder feignEncoder(
            @Qualifier("customServer-jacksonHttpMessageConverter") HttpMessageConverter<Object> httpMessageConverter) {
        return super.feignEncoder(httpMessageConverter);
    }

    @Override
    @Bean("customServer-feignDecoder")
    public Decoder feignDecoder(
            @Qualifier("customServer-jacksonHttpMessageConverter") HttpMessageConverter<Object> httpMessageConverter) {
        return super.feignDecoder(httpMessageConverter);
    }

    @Override
    @Bean("customServer-jacksonHttpMessageConverter")
    public HttpMessageConverter<Object> httpMessageConverter(
            @Qualifier("customServer-objectMapper") ObjectMapper objectMapper) {
        return super.httpMessageConverter(objectMapper);
    }

    @Override
    @Bean("customServer-feignErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig(
            @Qualifier("customServer-objectMapper") ObjectMapper objectMapper) {
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
    public CustomErrorDecoder customErrorDecoder(
            @Qualifier("customServer-feignErrorDecoderConfig") CustomErrorDecoderConfig customErrorDecoderConfig) {
        return super.customErrorDecoder(customErrorDecoderConfig);
    }

    @Override
    @Bean("customServer-httpClientBuilder")
    public HttpClientBuilder apacheHttpClientBuilder() {
        return super.apacheHttpClientBuilder();
    }

    @Override
    @Bean("customServer-retryer")
    @ConditionalOnMissingBean(name = {"customServer-retryer"})
    public Retryer retryer() {
        return super.retryer();
    }

    @Override
    @Bean("customServer-feignLoggerLevel")
    @ConditionalOnMissingBean(name = {"customServer-feignLoggerLevel"})
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
    @Bean("customServer-micrometerObservationCapability")
    public MicrometerObservationCapability capability(ObservationRegistry observationRegistry) {
        return super.capability(observationRegistry);
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
                                      @Qualifier("customServer-feignErrorDecoder") CustomErrorDecoder customErrorDecoder,
                                      @Qualifier("customServer-httpFeignClientLogger") Logger logger,
                                      @Qualifier("customServer-micrometerObservationCapability") Capability capability) {
        return super.feignBuilder(feignClient, options, requestInterceptors, feignContract, feignDecoder, feignEncoder,
                retryer, logLevel, customErrorDecoder, logger, capability);
    }

    @Bean
    public CustomServerRestController clientServerRestController(
            @Qualifier("customServer-clientConfig") HttpClientProperties customServerClientConfig,
            @Qualifier("customServer-feignBuilder") Feign.Builder feignBuilder) {
        return getFeignController(customServerClientConfig.getBaseServiceUrl(), CustomServerRestController.PATH,
                feignBuilder, CustomServerRestController.class);
    }
}
