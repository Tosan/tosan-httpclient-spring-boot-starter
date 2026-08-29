package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.ExternalServiceCircuitBreakerException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class ExternalServiceNameResolver {

    static final String SERVICE_NAME_FIELD = "SERVICE_NAME";
    private static final String NAME_SEPARATOR = ":";

    private ExternalServiceNameResolver() {
    }

    static CircuitBreakerNames resolve(Class<?> targetClass, CircuitBreaker annotation) {
        String providerName = resolveProviderName(targetClass);
        String circuitBreakerName = providerName;
        if (annotation != null && StringUtils.hasText(annotation.provider())) {
            circuitBreakerName = providerName + NAME_SEPARATOR + annotation.provider();
        }
        return new CircuitBreakerNames(providerName, circuitBreakerName);
    }

    private static String resolveProviderName(Class<?> targetClass) {
        ExternalServiceProvider providerAnnotation = targetClass.getAnnotation(ExternalServiceProvider.class);
        if (providerAnnotation != null
                && StringUtils.hasText(providerAnnotation.value())) {
            return providerAnnotation.value();
        }
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
            try {
                Field field = currentClass.getDeclaredField(SERVICE_NAME_FIELD);
                if (Modifier.isStatic(field.getModifiers())
                        && field.getType() == String.class) {
                    field.setAccessible(true);
                    return (String) field.get(null);
                }
            } catch (NoSuchFieldException ex) {
                // Field not declared on this class, keep walking up the hierarchy.
            } catch (IllegalAccessException ex) {
                throw new ExternalServiceCircuitBreakerException(
                        "Unable to read SERVICE_NAME from " + currentClass.getName(), ex);
            }

            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    record CircuitBreakerNames(String providerName, String circuitBreakerName) {
    }
}