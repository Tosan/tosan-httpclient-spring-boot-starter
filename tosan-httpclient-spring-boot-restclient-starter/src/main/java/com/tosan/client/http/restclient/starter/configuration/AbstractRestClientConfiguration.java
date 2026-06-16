package com.tosan.client.http.restclient.starter.configuration;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.factory.ConfigurableApacheHttpClientFactory;
import com.tosan.client.http.restclient.starter.exception.RestClientConfigurationException;
import com.tosan.client.http.restclient.starter.impl.ClientService;
import com.tosan.client.http.restclient.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.restclient.starter.impl.interceptor.HttpLoggingInterceptor;
import com.tosan.client.http.restclient.starter.util.HttpLoggingInterceptorUtil;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import io.micrometer.observation.ObservationRegistry;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractRestClientConfiguration<P extends HttpClientProperties> {

    private final String serviceName;
    private static final String DEFAULT_PROPERTIES_PATH = "client";
    private final ObservationRegistry observationRegistry;
    private final JsonReplaceHelperDecider jacksonReplaceHelper;
    private final RestClient.Builder builder;
    private final Class<P> propertiesClass;

    protected AbstractRestClientConfiguration(String serviceName, Class<P> propertiesClass, RestClient.Builder builder,
                                              ObservationRegistry observationRegistry,
                                              JsonReplaceHelperDecider jacksonReplaceHelper) {
        this.serviceName = serviceName;
        this.propertiesClass = propertiesClass;
        this.builder = builder;
        this.observationRegistry = observationRegistry;
        this.jacksonReplaceHelper = jacksonReplaceHelper;

    }

    protected final String getExternalServiceName() {
        return this.serviceName;
    }

    protected abstract ResponseErrorHandler createResponseErrorHandler();

    protected P loadHttpClientProperties(Environment environment) {
        String propertyPrefix =
                getExternalServiceName() + "." + pathProperties();
        return Binder.get(environment)
                .bind(propertyPrefix, Bindable.of(this.propertiesClass))
                .orElseThrow(() ->
                        new RestClientConfigurationException(
                                "Configuration not found for prefix: " + propertyPrefix
                        ));
    }

    protected String pathProperties() {
        return DEFAULT_PROPERTIES_PATH;
    }

    protected void validateProperties(P properties) {
        if (!StringUtils.hasText(properties.getBaseServiceUrl())) {
            throw new RestClientConfigurationException(
                    "Base service URL is required for service: "
                            + getExternalServiceName()
            );
        }
        try {
            URI.create(properties.getBaseServiceUrl());
        } catch (Exception ex) {
            throw new RestClientConfigurationException(
                    "Invalid base service URL for service: "
                            + getExternalServiceName(),
                    ex
            );
        }
    }

    protected DefaultClientRequestObservationConvention createObservationConvention() {
        return new TosanHttpClientObservationConvention().externalName(getExternalServiceName());
    }

    protected ClientService createClientService(P properties) {
        HttpComponentsClientHttpRequestFactory requestFactory = createRequestFactory(properties);
        RestClient.Builder builder = this.builder.clone();
        customizeRestClient(builder, properties);
        RestClient restClient = builder
                .configureMessageConverters(this::configureMessageConverters)
                .requestFactory(requestFactory)
                .requestInterceptors(interceptors ->
                        interceptors.addAll(createInterceptors(properties)))
                .defaultStatusHandler(createResponseErrorHandler())
                .observationRegistry(observationRegistry)
                .observationConvention(createObservationConvention())
                .build();
        return new ClientService(restClient, requestFactory);
    }

    protected void customizeRestClient(
            RestClient.Builder builder,
            P properties) {
    }

    protected CloseableHttpClient createHttpClient(P properties) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder =
                PoolingHttpClientConnectionManagerBuilder.create();
        ConfigurableApacheHttpClientFactory factory = new ConfigurableApacheHttpClientFactory(
                builder, connectionManagerBuilder, properties);
        return factory.createBuilder().build();
    }

    protected HttpComponentsClientHttpRequestFactory createRequestFactory(P properties) {
        return new HttpComponentsClientHttpRequestFactory(
                createHttpClient(properties)
        );
    }

    protected List<ClientHttpRequestInterceptor> createInterceptors(P properties) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(createLoggingInterceptor());
        HttpClientProperties.AuthorizationConfiguration auth = properties.getAuthorization();
        if (auth != null && auth.isEnable()) {
            interceptors.add(createBasicAuthInterceptor(properties));
        }
        return interceptors;
    }

    protected void configureMessageConverters(HttpMessageConverters.ClientBuilder converters) {
        converters.addCustomConverter(new JacksonJsonHttpMessageConverter());
    }

    private ClientHttpRequestInterceptor createLoggingInterceptor() {
        return new HttpLoggingInterceptor(new HttpLoggingInterceptorUtil(jacksonReplaceHelper), getExternalServiceName());
    }

    private ClientHttpRequestInterceptor createBasicAuthInterceptor(P properties) {
        HttpClientProperties.AuthorizationConfiguration authConfig = properties.getAuthorization();
        return new BasicAuthenticationInterceptor(
                authConfig.getUsername(),
                authConfig.getPassword(),
                StandardCharsets.UTF_8
        );
    }

    public final ExternalServiceInvoker createServiceInvoker(Environment environment) {
        P properties = loadHttpClientProperties(environment);
        validateProperties(properties);
        return new ExternalServiceInvoker(createClientService(properties), properties);
    }

    public final ExternalServiceInvoker createServiceInvoker(P properties) {
        validateProperties(properties);
        return new ExternalServiceInvoker(createClientService(properties), properties);
    }
}
