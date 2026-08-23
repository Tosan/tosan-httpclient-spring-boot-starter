package com.tosan.client.http.core.circuitbreaker;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    String provider() default "";
}
