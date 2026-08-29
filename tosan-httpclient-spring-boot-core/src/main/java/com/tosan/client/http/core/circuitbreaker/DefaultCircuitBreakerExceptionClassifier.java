package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.CircuitBreakerBusinessException;
import com.tosan.client.http.core.exception.InfrastructureFailureException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class DefaultCircuitBreakerExceptionClassifier
        implements CircuitBreakerExceptionClassifier {

    @Override
    public boolean shouldRecordFailure(Throwable throwable) {
        Set<Throwable> visited =
                Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = unwrap(throwable);
        while (current != null && visited.add(current)) {
            if (isIgnoredException(current)) {
                return false;
            }
            if (isFailureException(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isIgnoredException(Throwable throwable) {
        return throwable instanceof CallNotPermittedException
                || throwable instanceof CircuitBreakerBusinessException
                || isHttpClientError(throwable);
    }

    private boolean isFailureException(Throwable throwable) {
        return throwable instanceof InfrastructureFailureException
                || throwable instanceof ResourceAccessException
                || throwable instanceof HttpServerErrorException
                || throwable instanceof IOException;
    }

    private boolean isHttpClientError(Throwable throwable) {
        return throwable instanceof HttpClientErrorException exception
                && exception.getStatusCode().is4xxClientError();
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof UndeclaredThrowableException exception) {
            return exception.getUndeclaredThrowable();
        }
        return throwable;
    }
}

