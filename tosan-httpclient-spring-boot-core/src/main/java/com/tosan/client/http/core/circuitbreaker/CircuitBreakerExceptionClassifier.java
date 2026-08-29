package com.tosan.client.http.core.circuitbreaker;

public interface CircuitBreakerExceptionClassifier {

    boolean shouldRecordFailure(Throwable throwable);
}
