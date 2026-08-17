package com.tosan.client.http.restclient.starter.configuration;

import com.tosan.client.http.core.circuitbreaker.ExternalServiceCircuitBreakerRegistry;
import com.tosan.client.http.restclient.starter.impl.ExternalServiceInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class RestClientCircuitBreakerRegistrar implements BeanPostProcessor {

    private final ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry;

    RestClientCircuitBreakerRegistrar(ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExternalServiceInvoker externalServiceInvoker) {
            circuitBreakerRegistry.register(
                    beanName,
                    externalServiceInvoker.getHttpClientProperties().getCircuitBreaker());
        }
        return bean;
    }
}
