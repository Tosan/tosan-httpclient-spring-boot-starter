package com.tosan.client.http.core.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalServiceCircuitBreakerRegistry {
    private final CircuitBreakerExceptionClassifier exceptionClassifier;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Map<String, CircuitBreakerConfiguration> configurationByService =


            new ConcurrentHashMap<>();

    public ExternalServiceCircuitBreakerRegistry(
            CircuitBreakerExceptionClassifier exceptionClassifier) {
        this(exceptionClassifier, CircuitBreakerRegistry.ofDefaults());
    }

    ExternalServiceCircuitBreakerRegistry(
            CircuitBreakerExceptionClassifier exceptionClassifier,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.exceptionClassifier = exceptionClassifier;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void register(String serviceName, CircuitBreakerConfiguration configuration) {
        if (configuration == null || !configuration.isEnabled()) {
            return;
        }
        configurationByService.computeIfAbsent(serviceName, key -> configuration);
        circuitBreakerRegistry.circuitBreaker(
                serviceName,
                toResilienceConfig(configuration)
        );
    }

    public Optional<CircuitBreaker> resolve(String serviceName) {
        CircuitBreakerConfiguration configuration = configurationByService.get(serviceName);
        if (configuration == null || !configuration.isEnabled()) {
            return Optional.empty();
        }
        return Optional.of(
                circuitBreakerRegistry.circuitBreaker(
                        serviceName,
                        toResilienceConfig(configuration)));
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