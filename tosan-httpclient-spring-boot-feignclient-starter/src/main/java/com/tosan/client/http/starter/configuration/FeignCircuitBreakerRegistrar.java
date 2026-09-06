package com.tosan.client.http.starter.configuration;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.circuitbreaker.CircuitBreakerConfiguration;
import com.tosan.client.http.core.circuitbreaker.ExternalServiceCircuitBreakerRegistry;
import com.tosan.client.http.core.service.ExternalService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class FeignCircuitBreakerRegistrar implements BeanPostProcessor {

    private final ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry;

    FeignCircuitBreakerRegistrar(
            ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExternalService<?, ?> externalService) {
            String serviceName = externalService.getServiceName();
            HttpClientProperties properties = externalService.getProperties();
            CircuitBreakerConfiguration configuration = properties.getCircuitBreaker();
            if (configuration != null && configuration.isEnabled()) {
                circuitBreakerRegistry.register(serviceName, configuration);
            }
        }
        return bean;
    }
}