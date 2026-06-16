package com.tosan.client.http.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.factory.ConfigurableApacheHttpClientFactory;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.FeignBuilder;
import com.tosan.client.http.starter.impl.feign.ExternalServiceInvoker;
import com.tosan.client.http.starter.impl.feign.exception.FeignConfigurationException;
import com.tosan.client.http.starter.impl.feign.logger.HttpFeignClientLogger;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import feign.*;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.hc5.ApacheHttp5Client;
import feign.micrometer.MicrometerObservationCapability;
import io.micrometer.observation.ObservationRegistry;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tosan.client.http.core.Constants.*;

public abstract class AbstractFeignConfiguration<P extends HttpClientProperties> {

    private final String serviceName;
    private static final String DEFAULT_PROPERTIES_PATH = "client";
    private final ObservationRegistry observationRegistry;
    private final ObjectMapper defaultObjectMapper = createDefaultObjectMapper();
    private final JsonReplaceHelperDecider jsonReplaceHelperDecider;
    private final ObjectProvider<Feign.Builder> builderProvider;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Contract contract;
    private final Class<P> propertiesClass;

    protected AbstractFeignConfiguration(
            String serviceName, Class<P> propertiesClass, ObservationRegistry observationRegistry,
            JsonReplaceHelperDecider jacksonReplaceHelper,
            ObjectProvider<Feign.Builder> builderProvider, Encoder encoder, Decoder decoder, Contract contract
    ) {
        this.serviceName = serviceName;
        this.observationRegistry = observationRegistry;
        this.jsonReplaceHelperDecider = jacksonReplaceHelper;
        this.builderProvider = builderProvider;
        this.encoder = encoder;
        this.decoder = decoder;
        this.contract = contract;
        this.propertiesClass = propertiesClass;
    }

    protected final String getExternalServiceName() {
        return this.serviceName;
    }

    protected abstract CustomErrorDecoderConfig createCustomErrorDecoderConfig(ObjectMapper objectMapper);

    protected P loadHttpClientProperties(Environment environment) {
        String propertyPrefix =
                getExternalServiceName() + "." + pathProperties();
        return Binder.get(environment)
                .bind(propertyPrefix, Bindable.of(this.propertiesClass))
                .orElseThrow(() ->
                        new FeignConfigurationException(
                                "Configuration not found for prefix: " + propertyPrefix
                        ));
    }

    protected String pathProperties() {
        return DEFAULT_PROPERTIES_PATH;
    }

    protected ObjectMapper createObjectMapper() {
        return defaultObjectMapper;
    }

    protected Logger createLogger() {
        return new HttpFeignClientLogger(getExternalServiceName(), jsonReplaceHelperDecider);
    }

    protected Logger.Level getLogLevel() {
        return Logger.Level.FULL;
    }

    protected CloseableHttpClient createFeignHttpClient(P properties) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder =
                PoolingHttpClientConnectionManagerBuilder.create();
        ConfigurableApacheHttpClientFactory factory = new ConfigurableApacheHttpClientFactory(
                builder, connectionManagerBuilder, properties);
        return factory.createBuilder().build();
    }

    protected Client wrapHttpClient(CloseableHttpClient closeableHttpClient) {
        return new ApacheHttp5Client(closeableHttpClient);
    }

    protected List<RequestInterceptor> createRequestInterceptors(P properties) {
        List<RequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(createDefaultRequestInterceptor());
        if (properties.getAuthorization() != null && properties.getAuthorization().isEnable()) {
            interceptors.add(createBasicAuthInterceptor(properties));
        }
        return interceptors;
    }

    protected Encoder createEncoder(ObjectMapper objectMapper) {
        return encoder;
    }

    protected Decoder createDecoder(ObjectMapper objectMapper) {
        return decoder;
    }

    protected Contract createContract(ObjectMapper objectMapper) {
        return contract;
    }

    protected ErrorDecoder createErrorDecoder(ObjectMapper objectMapper) {
        CustomErrorDecoderConfig config = createCustomErrorDecoderConfig(objectMapper);
        return new CustomErrorDecoder(config);
    }

    protected Retryer createRetryer() {
        return Retryer.NEVER_RETRY;
    }

    protected Request.Options createRequestOptions(P properties) {
        HttpClientProperties.ConnectionConfiguration connectionConfig = properties.getConnection();
        return new Request.Options(
                connectionConfig.getConnectionTimeout(),
                TimeUnit.MILLISECONDS,
                connectionConfig.getSocketTimeout(),
                TimeUnit.MILLISECONDS,
                connectionConfig.isFollowRedirects()
        );
    }

    protected List<Capability> createCapabilities(ObservationRegistry observationRegistry) {
        TosanFeignObservationConvention convention = new TosanFeignObservationConvention()
                .externalName(getExternalServiceName());
        return List.of(new MicrometerObservationCapability(observationRegistry, convention));
    }

    protected FeignBuilder createFeignBuilder(P httpClientProperties) {
        CloseableHttpClient closeableHttpClient = createFeignHttpClient(httpClientProperties);
        ObjectMapper objectMapper = createObjectMapper();
        Feign.Builder feignBuilder = builderProvider.getIfAvailable();
        if (feignBuilder == null) {
            throw new FeignConfigurationException(
                    "No Feign.Builder bean found"
            );
        }
        feignBuilder = feignBuilder
                .client(wrapHttpClient(closeableHttpClient))
                .options(createRequestOptions(httpClientProperties))
                .encoder(createEncoder(objectMapper))
                .decoder(createDecoder(objectMapper))
                .errorDecoder(createErrorDecoder(objectMapper))
                .contract(createContract(objectMapper))
                .requestInterceptors(createRequestInterceptors(httpClientProperties))
                .retryer(createRetryer())
                .logger(createLogger())
                .logLevel(getLogLevel());
        createCapabilities(observationRegistry).forEach(feignBuilder::addCapability);
        return new FeignBuilder(feignBuilder, closeableHttpClient);
    }

    protected void validateProperties(P properties) {
        if (!StringUtils.hasText(properties.getBaseServiceUrl())) {
            throw new FeignConfigurationException(
                    "Base service URL is required for service: "
                            + getExternalServiceName()
            );
        }
        try {
            URI.create(properties.getBaseServiceUrl());
        } catch (Exception ex) {
            throw new FeignConfigurationException(
                    "Invalid base service URL for service: "
                            + getExternalServiceName(),
                    ex
            );
        }
    }

    private RequestInterceptor createDefaultRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(ACCEPT_HEADER, ContentType.APPLICATION_JSON.getMimeType());
            requestTemplate.header(CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.getMimeType());
            addMdcHeaderIfPresent(requestTemplate, MDC_REQUEST_ID, X_REQUEST_ID);
            addMdcHeaderIfPresent(requestTemplate, MDC_CLIENT_IP, X_USER_IP);
        };
    }

    private void addMdcHeaderIfPresent(RequestTemplate requestTemplate, String mdcKey, String headerName) {
        String mdcValue = MDC.get(mdcKey);
        if (mdcValue != null) {
            requestTemplate.header(headerName, mdcValue);
        }
    }

    private RequestInterceptor createBasicAuthInterceptor(P properties) {
        HttpClientProperties.AuthorizationConfiguration authConfig = properties.getAuthorization();
        return new BasicAuthRequestInterceptor(
                authConfig.getUsername(),
                authConfig.getPassword(),
                StandardCharsets.UTF_8
        );
    }

    private ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private String buildTargetUrl(P properties, String controllerPath) {
        String baseUrl = properties.getBaseServiceUrl();
        if (!StringUtils.hasText(controllerPath)) {
            return baseUrl;
        }
        return UriComponentsBuilder.fromUriString(baseUrl).path(controllerPath).build().toUriString();
    }

    protected final <T> ExternalServiceInvoker<T> createServiceInvoker(Environment environment, String controllerPath, Class<T> clientType) {
        P properties = loadHttpClientProperties(environment);
        validateProperties(properties);
        FeignBuilder feignBuilder = createFeignBuilder(properties);
        return new ExternalServiceInvoker<T>(
                feignBuilder.getFeignBuilder().target(clientType, buildTargetUrl(properties, controllerPath)),
                feignBuilder.getHttpClient()
        );
    }

    protected final <T> ExternalServiceInvoker<T> createServiceInvoker(Environment environment, Class<T> clientType) {
        return createServiceInvoker(environment, null, clientType);
    }

    protected final <T> ExternalServiceInvoker<T> createServiceInvoker(P properties, String controllerPath, Class<T> clientType) {
        validateProperties(properties);
        FeignBuilder feignBuilder = createFeignBuilder(properties);
        return new ExternalServiceInvoker<T>(
                feignBuilder.getFeignBuilder().target(clientType, buildTargetUrl(properties, controllerPath)),
                feignBuilder.getHttpClient()
        );
    }

    protected final <T> ExternalServiceInvoker<T> createServiceInvoker(P properties, Class<T> clientType) {
        return createServiceInvoker(properties, null, clientType);
    }
}
