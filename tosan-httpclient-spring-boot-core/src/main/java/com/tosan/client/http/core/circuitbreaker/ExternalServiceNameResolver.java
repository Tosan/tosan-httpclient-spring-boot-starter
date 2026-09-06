package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.ExternalServiceCircuitBreakerException;
import org.springframework.util.StringUtils;

final class ExternalServiceNameResolver {

    private ExternalServiceNameResolver() {
    }

    static String resolve(Class<?> targetClass, CircuitBreaker annotation) {
        String serviceName = readStaticServiceNameField(targetClass);
        if (StringUtils.hasText(serviceName)) {
            return serviceName;
        }
        throw new ExternalServiceCircuitBreakerException(
                "Unable to resolve external service name for " + targetClass.getName());
    }

    private static String readStaticServiceNameField(Class<?> targetClass) {
        Class<?> currentClass = targetClass;
        while (currentClass != null && currentClass != Object.class) {
        }
        return null;
    }
}
