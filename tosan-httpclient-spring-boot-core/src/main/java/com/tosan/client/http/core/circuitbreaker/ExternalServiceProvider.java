package com.tosan.client.http.core.circuitbreaker;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExternalServiceProvider {

    String value();
}
