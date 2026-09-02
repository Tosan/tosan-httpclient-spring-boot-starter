package com.tosan.client.http.sample.feignclient;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.service.ExternalService;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.InvalidParameterException;
import com.tosan.client.http.sample.server.api.exception.RequiredParameterException;
import com.tosan.client.http.sample.server.api.model.Context;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.exception.FeignClientRequestExecuteException;
import com.tosan.client.http.starter.impl.feign.exception.InternalServerException;
import com.tosan.client.http.starter.impl.feign.exception.UnknownException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.tosan.client.http.sample.feignclient", "com.tosan.client.http.sample.server.api"})
public class RestClientSpringBootApplication implements CommandLineRunner {

    @Autowired
    private ExternalService<
            CustomServerRestController, HttpClientProperties> externalService;

    @Autowired
    private CircuitBreakerFeignClientService circuitBreakerFeignClientService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(RestClientSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .build()
                .run();
    }

    /**
     * First works fine
     * Second works fine
     * Third must throw InvalidParameterException
     * Forth must throw RequiredParameterException
     * Fifth must throw NumberFormatException
     *
     * Circuit breaker scenarios (callErrorEndpoint):
     * - The first 3 calls fail with a 500, which opens the circuit breaker.
     * - Any further call is rejected with CallNotPermittedException without reaching the server.
     */
    @Override
    public void run(String... args) {
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("Test", "test");
        GetInfoRequestDto request = new GetInfoRequestDto();
        request.setSsn("123456789");
        GetInfoResponseDto response;
        try {
            response = externalService.getClient().getInfo(request, httpHeaders);
            log.info("FeignClient Info: {}", response.toString());
        } catch (InvalidParameterException e) {
            log.error("FeignClient Info exception:{}", e.toString());
        } catch (UnknownException e) {
            log.error("FeignClient Unknown exception with status Code 4xx:{}", e.toString());
        } catch (RequiredParameterException e) {
            log.error("FeignClient RequiredParameterException:{}", e.toString());
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        } catch (InternalServerException e) {
            log.error("InternalServerError Exception:", e);
        }

        try {
            Context context = new Context();
            context.setUsername("ali");
            context.setPassword("ali110");

            response = externalService.getClient().login(context);
            log.info("FeignClient Info: {}", response.toString());
        } catch (UnknownException e) {
            log.error("FeignClient Unknown exception with status Code 4xx:{}", e.toString());
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        } catch (InternalServerException e) {
            log.error("InternalServerError Exception:", e);
        }

        request.setSsn(null);
        try {
            response = externalService.getClient().getInfo(request, httpHeaders);
            log.info("FeignClient Info: {}", response.toString());
        } catch (InvalidParameterException e) {
            log.error("FeignClient Info exception:{}", e.toString());
        } catch (UnknownException e) {
            log.error("FeignClient Unknown exception with status Code 4xx:{}", e.toString());
        } catch (RequiredParameterException e) {
            log.error("FeignClient RequiredParameterException:{}", e.toString());
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        } catch (InternalServerException e) {
            log.error("InternalServerError Exception:", e);
        }

        request.setSsn("");
        try {
            response = externalService.getClient().getInfo(request, httpHeaders);
            log.info("FeignClient Info: {}", response.toString());
        } catch (InvalidParameterException e) {
            log.error("FeignClient Info exception:{}", e.toString());
        } catch (UnknownException e) {
            log.error("FeignClient Unknown exception with status Code 4xx:{}", e.toString());
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        } catch (RequiredParameterException e) {
            log.error("FeignClient RequiredParameterException:{}", e.toString());
        } catch (InternalServerException e) {
            log.error("InternalServerError Exception:", e);
        }

        request.setSsn("a1233");
        try {
            response = externalService.getClient().getInfo(request, httpHeaders);
            log.info("FeignClient Info: {}", response.toString());
        } catch (InvalidParameterException e) {
            log.error("FeignClient Info exception:{}", e.toString());
        } catch (NumberFormatException e) {
            log.error("FeignClient NumberFormatException with status Code 5xx:{}", e.toString());
        } catch (RequiredParameterException e) {
            log.error("FeignClient RequiredParameterException:{}", e.toString());
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        } catch (InternalServerException e) {
            log.error("InternalServerError Exception:{}", e.toString());
        }

        GetInfoRequestDto validRequest = new GetInfoRequestDto();
        validRequest.setSsn("123456789");
        try {
            response = circuitBreakerFeignClientService.callGetInfo(validRequest, httpHeaders);
            log.info("[CB] Successful call -> Response: {}", response);
        } catch (Exception e) {
            log.error("[CB] Unexpected exception on successful call:", e);
        }

        for (int i = 1; i <= 5; i++) {
            try {
                circuitBreakerFeignClientService.callErrorEndpoint();
            } catch (CallNotPermittedException e) {
                log.warn("[CB] Call {} rejected because circuit breaker is OPEN (CallNotPermittedException)", i);
            } catch (Exception e) {
                log.warn("[CB] Call {} recorded as failure: {}", i, e.getClass().getSimpleName());
            }
        }

        // Once OPEN, even a valid request is rejected without reaching the server.
        try {
            response = circuitBreakerFeignClientService.callGetInfo(validRequest, httpHeaders);
            log.info("[CB] Response: {}", response);
        } catch (CallNotPermittedException e) {
            log.warn("[CB] Valid call rejected while circuit breaker is OPEN -> {}", e.getMessage());
        } catch (Exception e) {
            log.error("[CB] Unexpected exception:", e);
        }
    }
}