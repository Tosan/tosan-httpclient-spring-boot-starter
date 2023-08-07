package com.tosan.client.http.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tosan.client.http.core.Constants;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.factory.ConfigurableApacheHttpClientFactory;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.exception.FeignConfigurationException;
import com.tosan.client.http.starter.impl.feign.logger.HttpFeignClientLogger;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import com.tosan.tools.mask.starter.replace.JacksonReplaceHelper;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import com.tosan.tools.mask.starter.replace.RegexReplaceHelper;
import feign.*;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.tosan.tools.mask.starter.configuration.MaskBeanConfiguration.SECURED_PARAMETERS;

/**
 * @author Ali Alimohammadi
 * @since 7/19/2022
 */
public abstract class AbstractFeignConfiguration {
    protected ObjectFactory<HttpMessageConverters> messageConverters;

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    public JsonReplaceHelperDecider replaceHelperDecider(JacksonReplaceHelper jacksonReplaceHelper,
                                                         RegexReplaceHelper regexReplaceHelper,
                                                         SecureParametersConfig secureParametersConfig) {
        return new JsonReplaceHelperDecider(jacksonReplaceHelper, regexReplaceHelper, secureParametersConfig);
    }

    public SecureParametersConfig secureParametersConfig() {
        HashSet<SecureParameter> securedParameters = new HashSet<>(SECURED_PARAMETERS);
        securedParameters.add(Constants.AUTHORIZATION_SECURE_PARAM);
        securedParameters.add(Constants.PROXY_AUTHORIZATION_SECURE_PARAM);
        return new SecureParametersConfig(securedParameters);
    }

    public Logger httpFeignClientLogger(JsonReplaceHelperDecider replaceHelperDecider, String serverName) {
        return new HttpFeignClientLogger(serverName, replaceHelperDecider);
    }

    public ApacheHttpClientFactory apacheHttpClientFactory(HttpClientBuilder builder,
                                                           ApacheHttpClientConnectionManagerFactory clientConnectionManagerFactory,
                                                           HttpClientProperties customServerClientConfig) {
        return new ConfigurableApacheHttpClientFactory(builder, clientConnectionManagerFactory, customServerClientConfig);
    }

    public ClientHttpRequestFactory clientHttpRequestFactory(ApacheHttpClientFactory apacheHttpClientFactory) {
        return new HttpComponentsClientHttpRequestFactory(apacheHttpClientFactory.createBuilder().build());
    }

    public CloseableHttpClient httpClient(ApacheHttpClientFactory apacheHttpClientFactory) {
        return apacheHttpClientFactory.createBuilder().build();
    }

    public ApacheHttpClientConnectionManagerFactory connectionManagerFactory() {
        return new DefaultApacheHttpClientConnectionManagerFactory();
    }

    public Client feignClient(HttpClient httpClient) {
        return new ApacheHttpClient(httpClient);
    }

    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Accept", ContentType.APPLICATION_JSON.getMimeType());
            requestTemplate.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        };
    }

    public List<RequestInterceptor> requestInterceptors(HttpClientProperties customServerClientConfig,
                                                        RequestInterceptor requestInterceptor) {
        List<RequestInterceptor> requestInterceptors = new ArrayList<>();
        requestInterceptors.add(requestInterceptor);
        HttpClientProperties.AuthorizationConfiguration authorizationConfiguration =
                customServerClientConfig.getAuthorization();
        if (customServerClientConfig.getAuthorization().isEnable()) {
            requestInterceptors.add(new BasicAuthRequestInterceptor(authorizationConfiguration.getUsername(),
                    authorizationConfiguration.getPassword(), StandardCharsets.UTF_8));
        }
        return requestInterceptors;
    }

    public Contract feignContract() {
        return new SpringMvcContract();
    }

    public Contract feignContractWithCustomSpringConversion(ConversionService feignConversionService,
                                                            List<AnnotatedParameterProcessor> processors) {
        return new SpringMvcContract(processors, feignConversionService);
    }

    public FormattingConversionService feignConversionService(List<FeignFormatterRegistrar> feignFormatterRegistrars) {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        for (FeignFormatterRegistrar feignFormatterRegistrar : feignFormatterRegistrars) {
            feignFormatterRegistrar.registerFormatters(conversionService);
        }
        return conversionService;
    }

    public Encoder feignEncoder(HttpMessageConverter<Object> httpMessageConverter) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    public Decoder feignDecoder(HttpMessageConverter<Object> httpMessageConverter) {
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    }

    public HttpMessageConverter<Object> httpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        messageConverters = () -> new HttpMessageConverters(mappingJackson2HttpMessageConverter);
        return mappingJackson2HttpMessageConverter;
    }


    public abstract CustomErrorDecoderConfig customErrorDecoderConfig(ObjectMapper objectMapper);


    public CustomErrorDecoder customErrorDecoder(CustomErrorDecoderConfig customErrorDecoderConfig) {
        return new CustomErrorDecoder(customErrorDecoderConfig);
    }

    public HttpClientBuilder apacheHttpClientBuilder() {
        return HttpClientBuilder.create();
    }

    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }


    public Request.Options options(HttpClientProperties customServerClientConfig) {
        HttpClientProperties.ConnectionConfiguration connectionConfiguration = customServerClientConfig
                .getConnection();
        return new Request.Options(
                connectionConfiguration.getConnectionTimeout(), TimeUnit.MILLISECONDS,
                connectionConfiguration.getSocketTimeout(), TimeUnit.MILLISECONDS, connectionConfiguration
                .isFollowRedirects());
    }

    public Feign.Builder feignBuilder(Client feignClient,
                                      Request.Options options,
                                      List<RequestInterceptor> requestInterceptors,
                                      Contract feignContract,
                                      Decoder feignDecoder,
                                      Encoder feignEncoder,
                                      Retryer retryer,
                                      Logger.Level logLevel,
                                      CustomErrorDecoder customErrorDecoder,
                                      Logger logger) {
        return Feign.builder().client(feignClient)
                .options(options)
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .errorDecoder(customErrorDecoder)
                .contract(feignContract)
                .requestInterceptors(requestInterceptors)
                .retryer(retryer)
                .logger(logger)
                .logLevel(logLevel);
    }

    protected final <T> T getFeignController(String baseServiceUrl, String controllerPath, Feign.Builder feignBuilder,
                                             Class<T> classType) {
        return createFeignController(baseServiceUrl, controllerPath, feignBuilder, classType);
    }

    protected final <T> T getFeignController(String baseServiceUrl, String controllerPath, Feign.Builder feignBuilder,
                                             Class<T> classType, Logger logger) {
        return createFeignController(baseServiceUrl, controllerPath, feignBuilder, classType, logger);
    }

    protected final <T> T getFeignController(String baseServiceUrl, Feign.Builder feignBuilder,
                                             Class<T> classType) {
        return createFeignController(baseServiceUrl, null, feignBuilder, classType);
    }

    protected final <T> T getFeignController(String baseServiceUrl, Feign.Builder feignBuilder,
                                             Class<T> classType, Logger logger) {
        return createFeignController(baseServiceUrl, null, feignBuilder, classType, logger);
    }

    private <T> T createFeignController(String baseServiceUrl, String controllerPath, Feign.Builder feignBuilder,
                                        Class<T> classType, Logger logger) {
        if (baseServiceUrl == null) {
            throw new FeignConfigurationException("base service url for feign client can not be null.");
        }

        return feignBuilder
                .logger(logger)
                .target(classType, controllerPath != null ? baseServiceUrl + controllerPath : baseServiceUrl);
    }

    private <T> T createFeignController(String baseServiceUrl, String controllerPath, Feign.Builder feignBuilder,
                                        Class<T> classType) {
        if (baseServiceUrl == null) {
            throw new FeignConfigurationException("base service url for feign client can not be null.");
        }

        return feignBuilder
                .target(classType, controllerPath != null ? baseServiceUrl + controllerPath : baseServiceUrl);
    }
}
