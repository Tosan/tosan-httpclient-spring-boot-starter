package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.InfrastructureFailureException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExternalServiceCircuitBreakerRegistryUTest {

    private final CircuitBreakerExceptionClassifier exceptionClassifier = new DefaultCircuitBreakerExceptionClassifier();

    @Test
    public void distinctServiceNames_resolveToDistinctCircuitBreakers() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "true")
                .withProperty("circuit-breaker.minimum-number-of-calls", "1")
                .withProperty("circuit-breaker.sliding-window-size", "2")
                .withProperty("circuit-breaker.failure-rate-threshold", "50")
                .withProperty("circuit-breaker.wait-duration-in-open-state", "1s");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        Optional<CircuitBreaker> serviceA = registry.resolve("web-service-a");
        Optional<CircuitBreaker> serviceB = registry.resolve("web-service-b");

        assertThat(serviceA).isPresent();
        assertThat(serviceB).isPresent();
        assertThat(serviceA.get()).isNotSameAs(serviceB.get());
    }

    @Test
    public void sameServiceName_resolvesToSameCircuitBreaker() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "true");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        Optional<CircuitBreaker> first = registry.resolve("web-service-a");
        Optional<CircuitBreaker> second = registry.resolve("web-service-a");

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get()).isSameAs(second.get());
    }

    @Test
    public void failureInOneService_doesNotOpenTheOtherServiceCircuitBreaker() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "true")
                .withProperty("circuit-breaker.minimum-number-of-calls", "1")
                .withProperty("circuit-breaker.sliding-window-size", "2")
                .withProperty("circuit-breaker.failure-rate-threshold", "50")
                .withProperty("circuit-breaker.wait-duration-in-open-state", "1s");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        CircuitBreaker serviceA = registry.resolve("web-service-a").orElseThrow();
        CircuitBreaker serviceB = registry.resolve("web-service-b").orElseThrow();

        assertThatThrownBy(() -> serviceA.executeCheckedSupplier(() -> {
            throw new InfrastructureFailureException("boom");
        })).isInstanceOf(InfrastructureFailureException.class);

        assertThat(serviceA.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(serviceB.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        String result = serviceB.executeCheckedSupplier(() -> "ok");
        assertThat(result).isEqualTo("ok");

        assertThatThrownBy(() -> serviceA.executeCheckedSupplier(() -> "blocked"))
                .isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
    }

    @Test
    public void sharedConfiguration_usedWhenNoPerServiceConfigurationExists() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "true")
                .withProperty("circuit-breaker.minimum-number-of-calls", "7")
                .withProperty("circuit-breaker.sliding-window-size", "12");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        CircuitBreaker serviceA = registry.resolve("web-service-a").orElseThrow();
        CircuitBreaker serviceB = registry.resolve("web-service-b").orElseThrow();

        assertThat(serviceA.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(7);
        assertThat(serviceA.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(12);
        assertThat(serviceB.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(7);
        assertThat(serviceB.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(12);
    }

    @Test
    public void perServiceConfiguration_overridesSharedConfiguration() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "true")
                .withProperty("circuit-breaker.minimum-number-of-calls", "7")
                .withProperty("web-service-a.client.circuit-breaker.enabled", "true")
                .withProperty("web-service-a.client.circuit-breaker.minimum-number-of-calls", "3")
                .withProperty("web-service-a.client.circuit-breaker.sliding-window-size", "4");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        CircuitBreaker serviceA = registry.resolve("web-service-a").orElseThrow();
        CircuitBreaker serviceB = registry.resolve("web-service-b").orElseThrow();

        assertThat(serviceA.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(3);
        assertThat(serviceA.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(4);
        assertThat(serviceB.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(7);
    }

    @Test
    public void disabledSharedConfiguration_returnsEmpty() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("circuit-breaker.enabled", "false");

        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier, environment);

        assertThat(registry.resolve("web-service-a")).isEmpty();
    }

    @Test
    public void noConfiguration_returnsEmpty() {
        ExternalServiceCircuitBreakerRegistry registry =
                new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);

        assertThat(registry.resolve("web-service-a")).isEmpty();
    }
}