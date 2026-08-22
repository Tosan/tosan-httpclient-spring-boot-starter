package com.tosan.client.http.restclient.starter.configuration;

import com.tosan.client.http.core.circuitbreaker.CircuitBreakerConfiguration;
import com.tosan.client.http.core.circuitbreaker.ExternalServiceCircuitBreakerRegistry;
import com.tosan.client.http.restclient.starter.impl.ExternalServiceInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;

public class RestClientCircuitBreakerRegistrar implements BeanPostProcessor {

    private final ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry;
    private final Environment environment;

    RestClientCircuitBreakerRegistrar(
            ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry,
            Environment environment) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExternalServiceInvoker externalServiceInvoker) {
            CircuitBreakerConfiguration configuration =
                    circuitBreakerRegistry.bindConfiguration(beanName, environment);
            if (configuration != null) {
                circuitBreakerRegistry.register(beanName, configuration);
            }
        }
        return bean;
    }
}
