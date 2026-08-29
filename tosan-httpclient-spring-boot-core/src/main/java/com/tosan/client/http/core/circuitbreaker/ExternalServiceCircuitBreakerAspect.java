package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.ExternalServiceCircuitBreakerException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;

@Aspect
public class ExternalServiceCircuitBreakerAspect {

    private final ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry;

    public ExternalServiceCircuitBreakerAspect(
            ExternalServiceCircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Around("@annotation(breaker)")
    public Object applyCircuitBreaker(ProceedingJoinPoint joinPoint, CircuitBreaker breaker) throws Throwable {
        ExternalServiceNameResolver.CircuitBreakerNames names =
                ExternalServiceNameResolver.resolve(
                        joinPoint.getTarget().getClass(),
                        breaker);
        Optional<io.github.resilience4j.circuitbreaker.CircuitBreaker> circuitBreaker =
                circuitBreakerRegistry.resolve(
                        names.circuitBreakerName(),
                        names.providerName());
        if (circuitBreaker.isEmpty()) {
            return joinPoint.proceed();
        }
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();
        return circuitBreaker.get().executeCheckedSupplier(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                if (throwable instanceof Exception exception
                        && signature.getMethod().getExceptionTypes().length == 0) {
                    throw exception;
                }
                if (throwable instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (throwable instanceof Error error) {
                    throw error;
                }
                throw new ExternalServiceCircuitBreakerException(
                        "Unexpected checked exception during circuit breaker protected invocation",
                        throwable);
            }
        });
    }
}