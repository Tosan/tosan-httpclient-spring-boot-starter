package com.tosan.client.http.core.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalServiceCircuitBreakerRegistry {

    static final String DEFAULT_PROPERTIES_PATH = "client";
    static final String CIRCUIT_BREAKER_PROPERTIES_SUFFIX = ".circuit-breaker";
    static final String SHARED_CIRCUIT_BREAKER_PROPERTIES_PATH = "circuit-breaker";
    private final CircuitBreakerExceptionClassifier exceptionClassifier;
    private final Environment environment;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Map<String, CircuitBreakerConfiguration> configurationByProvider =
            new ConcurrentHashMap<>();

    public ExternalServiceCircuitBreakerRegistry(
            CircuitBreakerExceptionClassifier exceptionClassifier) {
        this(exceptionClassifier, null, CircuitBreakerRegistry.ofDefaults());
    }

    public ExternalServiceCircuitBreakerRegistry(CircuitBreakerExceptionClassifier exceptionClassifier,
                                                 Environment environment) {
        this(
                exceptionClassifier,
                environment,
                CircuitBreakerRegistry.ofDefaults());
    }

    ExternalServiceCircuitBreakerRegistry(
            CircuitBreakerExceptionClassifier exceptionClassifier,
            Environment environment,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.exceptionClassifier = exceptionClassifier;
        this.environment = environment;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void register(String serviceName, CircuitBreakerConfiguration configuration) {
        if (configuration == null) {
            return;
        }
        configurationByProvider.put(serviceName, configuration);
        if (configuration.isEnabled()) {
            circuitBreakerRegistry.circuitBreaker(
                    serviceName,
                    toResilienceConfig(configuration));
        }
    }

    public Optional<CircuitBreaker> resolve(String circuitBreakerName, String configurationName) {
        CircuitBreakerConfiguration configuration = configurationByProvider.get(configurationName);
        if (configuration == null) {
            configuration = resolveConfiguration(configurationName);
            if (configuration != null) {
                configurationByProvider.put(
                        configurationName,
                        configuration);
            }
        }
        if (configuration == null || !configuration.isEnabled()) {
            return Optional.empty();
        }
        return Optional.of(circuitBreakerRegistry.circuitBreaker(
                circuitBreakerName,
                toResilienceConfig(configuration)));
    }

    public CircuitBreakerConfiguration bindConfiguration(String serviceName, Environment environment) {
        String propertyPrefix =
                serviceName
                        + "."
                        + DEFAULT_PROPERTIES_PATH
                        + CIRCUIT_BREAKER_PROPERTIES_SUFFIX;
        return Binder.get(environment)
                .bind(
                        propertyPrefix,
                        Bindable.of(CircuitBreakerConfiguration.class))
                .orElse(null);
    }

    public CircuitBreakerConfiguration bindSharedConfiguration(Environment environment) {
        return Binder.get(environment)
                .bind(
                        SHARED_CIRCUIT_BREAKER_PROPERTIES_PATH,
                        Bindable.of(CircuitBreakerConfiguration.class))
                .orElse(null);
    }

    private CircuitBreakerConfiguration resolveConfiguration(String serviceName) {
        if (environment == null) {
            return null;
        }
        CircuitBreakerConfiguration perServiceConfiguration =
                bindConfiguration(serviceName, environment);
        if (perServiceConfiguration != null) {
            return perServiceConfiguration;
        }
        return bindSharedConfiguration(environment);
    }

    private CircuitBreakerConfig toResilienceConfig(
            CircuitBreakerConfiguration configuration) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(
                        configuration.getFailureRateThreshold())
                .minimumNumberOfCalls(
                        configuration.getMinimumNumberOfCalls())
                .slidingWindowSize(
                        configuration.getSlidingWindowSize())
                .waitDurationInOpenState(
                        configuration.getWaitDurationInOpenState())
                .recordException(
                        exceptionClassifier::shouldRecordFailure)
                .build();
    }
}