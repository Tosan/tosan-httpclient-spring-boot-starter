package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.InfrastructureFailureException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalServiceCircuitBreakerRegistryUTest {

    private final CircuitBreakerExceptionClassifier exceptionClassifier =
            new DefaultCircuitBreakerExceptionClassifier();

    @Test
    void distinctServiceNames_resolveToDistinctCircuitBreakers() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        registry.register("web-service-a", configuration());
        registry.register("web-service-b", configuration());
        Optional<CircuitBreaker> serviceA = registry.resolve("web-service-a");
        Optional<CircuitBreaker> serviceB = registry.resolve("web-service-b");
        assertThat(serviceA).isPresent();
        assertThat(serviceB).isPresent();
        assertThat(serviceA.get()).isNotSameAs(serviceB.get());
    }

    @Test
    void sameServiceName_resolvesToSameCircuitBreaker() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        registry.register("web-service-a", configuration());
        Optional<CircuitBreaker> first = registry.resolve("web-service-a");
        Optional<CircuitBreaker> second = registry.resolve("web-service-a");
        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get()).isSameAs(second.get());
    }

    @Test
    void failureInOneService_doesNotOpenTheOtherServiceCircuitBreaker()
            throws Throwable {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        registry.register("web-service-a", configuration());
        registry.register("web-service-b", configuration());
        CircuitBreaker serviceA = registry.resolve("web-service-a").orElseThrow();
        CircuitBreaker serviceB = registry.resolve("web-service-b").orElseThrow();
        assertThatThrownBy(() ->
                serviceA.executeCheckedSupplier(() -> {
                    throw new InfrastructureFailureException("boom");
                })
        ).isInstanceOf(InfrastructureFailureException.class);
        assertThat(serviceA.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(serviceB.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        String result = serviceB.executeCheckedSupplier(() -> "ok");
        assertThat(result).isEqualTo("ok");
        assertThatThrownBy(() -> serviceA.executeCheckedSupplier(() -> "blocked")
        ).isInstanceOf(
                io.github.resilience4j.circuitbreaker.CallNotPermittedException.class
        );
    }

    @Test
    void disabledConfiguration_returnsEmpty() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        CircuitBreakerConfiguration configuration = configuration();
        configuration.setEnabled(false);
        registry.register("web-service-a", configuration);
        assertThat(registry.resolve("web-service-a"))
                .isEmpty();
    }

    @Test
    void nullConfiguration_returnsEmpty() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        registry.register("web-service-a", null);
        assertThat(registry.resolve("web-service-a"))
                .isEmpty();
    }

    @Test
    void noRegistration_returnsEmpty() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
        assertThat(registry.resolve("web-service-a"))
                .isEmpty();
    }

    private CircuitBreakerConfiguration configuration() {
        CircuitBreakerConfiguration configuration =
                new CircuitBreakerConfiguration();
        configuration.setEnabled(true);
        configuration.setFailureRateThreshold(50);
        configuration.setMinimumNumberOfCalls(1);
        configuration.setSlidingWindowSize(2);
        configuration.setWaitDurationInOpenState(
                Duration.ofSeconds(1)
        );
        return configuration;
    }
}