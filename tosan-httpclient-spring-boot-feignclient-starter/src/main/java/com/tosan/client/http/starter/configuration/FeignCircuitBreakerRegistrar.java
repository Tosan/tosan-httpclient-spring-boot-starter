package com.tosan.client.http.starter.configuration;

import com.tosan.client.http.core.circuitbreaker.CircuitBreakerConfiguration;
import com.tosan.client.http.core.circuitbreaker.ExternalServiceCircuitBreakerRegistry;
import com.tosan.client.http.starter.impl.feign.ExternalServiceInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;

public class FeignCircuitBreakerRegistrar implements BeanPostProcessor {

    private final ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry;
    private final Environment environment;

    FeignCircuitBreakerRegistrar(
            ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry,
            Environment environment) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExternalServiceInvoker<?>) {
            CircuitBreakerConfiguration configuration =
                    circuitBreakerRegistry.bindConfiguration(beanName, environment);
            if (configuration != null) {
                circuitBreakerRegistry.register(beanName, configuration);
            }
        }
        return bean;
    }
}
