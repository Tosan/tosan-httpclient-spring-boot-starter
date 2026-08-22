package com.tosan.client.http.core.circuitbreaker;

import com.tosan.client.http.core.exception.CircuitBreakerBusinessException;
import com.tosan.client.http.core.exception.InfrastructureFailureException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultCircuitBreakerExceptionClassifier
        implements CircuitBreakerExceptionClassifier {

    private static final String FEIGN_EXCEPTION_NAME = "feign.FeignException";
    private static final String INTERNAL_SERVER_EXCEPTION_NAME = "com.tosan.client.http.starter.impl.feign.exception.InternalServerException";
    private static final String UNKNOWN_EXCEPTION_NAME = "com.tosan.client.http.starter.impl.feign.exception.UnknownException";

    @Override
    public boolean shouldRecordFailure(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
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
        if (throwable instanceof CallNotPermittedException
                || throwable instanceof CircuitBreakerBusinessException) {
            return true;
        }
        if (throwable.getClass().getName().equals(UNKNOWN_EXCEPTION_NAME)) {
            return true;
        }
        if (throwable instanceof HttpClientErrorException exception) {
            return exception.getStatusCode().is4xxClientError();
        }
        return isFeignClientError(throwable);
    }

    private boolean isFailureException(Throwable throwable) {
        if (throwable instanceof InfrastructureFailureException
                || throwable instanceof ResourceAccessException
                || throwable instanceof HttpServerErrorException
                || throwable instanceof IOException
                || throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof UnknownHostException) {
            return true;
        }
        if (isInternalServerError(throwable)) {
            return true;
        }
        return isFeignServerError(throwable);
    }

    private boolean isInternalServerError(Throwable throwable) {
        String name = throwable.getClass().getName();
        if (name.equals(INTERNAL_SERVER_EXCEPTION_NAME)) {
            // InternalServerException from feign starter - check httpStatusCode
            try {
                var errorParam = throwable.getClass().getMethod("getErrorParam").invoke(throwable);
                if (errorParam instanceof Map) {
                    Object httpStatus = ((Map<?, ?>) errorParam).get("httpStatusCode");
                    if (httpStatus instanceof Number && (Integer) httpStatus >= 500) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean isFeignClientError(Throwable throwable) {
        if (!isFeignException(throwable)) {
            return false;
        }
        Integer status = getFeignStatus(throwable);
        return status != null && status >= 400 && status < 500;
    }

    private boolean isFeignServerError(Throwable throwable) {
        if (!isFeignException(throwable)) {
            return false;
        }
        Integer status = getFeignStatus(throwable);
        return status != null && (status >= 500 || status < 0);
    }

    private boolean isFeignException(Throwable throwable) {
        String name = throwable.getClass().getName();
        return name.equals(FEIGN_EXCEPTION_NAME)
                || name.startsWith(FEIGN_EXCEPTION_NAME + "$");
    }

    private Integer getFeignStatus(Throwable throwable) {
        try {
            var method = throwable.getClass().getMethod("status");
            return (Integer) method.invoke(throwable);
        } catch (Exception e) {
            return null;
        }
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof UndeclaredThrowableException e) {
            return e.getUndeclaredThrowable();
        }
        return throwable;
    }
}
