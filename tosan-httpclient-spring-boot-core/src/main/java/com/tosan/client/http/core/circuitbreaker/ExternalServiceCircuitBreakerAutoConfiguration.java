package com.tosan.client.http.core.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;


@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnClass(CircuitBreaker.class)
public class ExternalServiceCircuitBreakerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CircuitBreakerExceptionClassifier.class)
    public CircuitBreakerExceptionClassifier circuitBreakerExceptionClassifier() {
        return new DefaultCircuitBreakerExceptionClassifier();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalServiceCircuitBreakerRegistry externalServiceCircuitBreakerRegistry(
            CircuitBreakerExceptionClassifier exceptionClassifier,
            Environment environment) {
        return new ExternalServiceCircuitBreakerRegistry(exceptionClassifier);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalServiceCircuitBreakerAspect externalServiceCircuitBreakerAspect(
            ExternalServiceCircuitBreakerRegistry registry) {
        return new ExternalServiceCircuitBreakerAspect(registry);
    }
}
