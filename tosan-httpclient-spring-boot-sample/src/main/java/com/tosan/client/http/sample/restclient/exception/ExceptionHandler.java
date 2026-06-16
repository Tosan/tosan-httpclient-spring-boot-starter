package com.tosan.client.http.sample.restclient.exception;

import com.tosan.client.http.restclient.starter.impl.interceptor.AbstractErrorHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

/**
 * @author Ali Alimohammadi
 * @since 8/16/2022
 */
@Slf4j
public class ExceptionHandler extends AbstractErrorHandler {

    @Override
    public void mappingException(Exception exception) {
        if(exception instanceof HttpClientErrorException) {
            throw new HttpClientRequestWrapperException("nok1",exception);
        } else if (exception instanceof HttpServerErrorException) {
            throw new HttpClientRequestWrapperException("nok2",exception);
        } else if (exception instanceof UnknownHttpStatusCodeException) {
            throw new HttpClientRequestWrapperException("nok3",exception);
        }
    }
}
