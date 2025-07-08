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
import feign.hc5.ApacheHttp5Client;
import feign.micrometer.MicrometerObservationCapability;
import io.micrometer.observation.ObservationRegistry;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tosan.client.http.core.Constants.*;
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

    public ConfigurableApacheHttpClientFactory apacheHttpClientFactory(
            HttpClientBuilder builder,
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder,
            HttpClientProperties customServerClientConfig) {
        return new ConfigurableApacheHttpClientFactory(builder, connectionManagerBuilder, customServerClientConfig);
    }

    public ClientHttpRequestFactory clientHttpRequestFactory(ConfigurableApacheHttpClientFactory apacheHttpClientFactory) {
        return new HttpComponentsClientHttpRequestFactory(apacheHttpClientFactory.createBuilder().build());
    }

    public CloseableHttpClient httpClient(ConfigurableApacheHttpClientFactory apacheHttpClientFactory) {
        //todo: closeable
        return apacheHttpClientFactory.createBuilder().build();
    }

    public PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder() {
        return PoolingHttpClientConnectionManagerBuilder.create();
    }

    public Client feignClient(HttpClient httpClient) {
        return new ApacheHttp5Client(httpClient);
    }

    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(ACCEPT_HEADER, ContentType.APPLICATION_JSON.getMimeType());
            requestTemplate.header(CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.getMimeType());
            if (MDC.get(MDC_REQUEST_ID) != null) {
                requestTemplate.header(X_REQUEST_ID, MDC.get(MDC_REQUEST_ID));
            }
            if (MDC.get(MDC_CLIENT_IP) != null) {
                requestTemplate.header(X_USER_IP, MDC.get(MDC_CLIENT_IP));
            }
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

    public List<Capability> feignCapabilities(ObservationRegistry observationRegistry) {
        return List.of(new MicrometerObservationCapability(observationRegistry));
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
                                      Logger logger,
                                      List<Capability> feignCapabilities) {
        Feign.Builder feignBuilder = Feign.builder().client(feignClient)
                .options(options)
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .errorDecoder(customErrorDecoder)
                .contract(feignContract)
                .requestInterceptors(requestInterceptors)
                .retryer(retryer)
                .logger(logger)
                .logLevel(logLevel);
        Optional.ofNullable(feignCapabilities)
                .stream()
                .flatMap(Collection::stream)
                .forEach(feignBuilder::addCapability);
        return feignBuilder;
    }

    protected final <T> T getFeignController(String baseServiceUrl, String controllerPath, Feign.Builder feignBuilder,
                                             Class<T> classType) {
        return createFeignController(baseServiceUrl, controllerPath, feignBuilder, classType);
    }

    protected final <T> T getFeignController(String baseServiceUrl, Feign.Builder feignBuilder, Class<T> classType) {
        return createFeignController(baseServiceUrl, null, feignBuilder, classType);
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
