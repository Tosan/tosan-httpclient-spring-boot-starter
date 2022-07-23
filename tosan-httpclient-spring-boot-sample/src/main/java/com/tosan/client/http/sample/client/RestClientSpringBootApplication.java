package com.tosan.client.http.sample.client;

import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.InvalidParameterException;
import com.tosan.client.http.sample.server.api.exception.RequiredParameterException;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.exception.FeignClientRequestExecuteException;
import com.tosan.client.http.starter.impl.feign.exception.InternalServerException;
import com.tosan.client.http.starter.impl.feign.exception.UnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpHeaders;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.tosan.client.http.sample.client", "com.tosan.client.http.sample.server.api"})
public class RestClientSpringBootApplication implements CommandLineRunner {

    @Autowired
    private CustomServerRestController customServerClient;

    public static void main(String[] args) {
        new SpringApplicationBuilder(RestClientSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .build()
                .run();
    }

    /**
     * First works fine
     * Second must throw InvalidParameterException
     * Third must throw RequiredParameterException
     * Forth must throw InternalServerError
     * Fifth must throw Unknown exception
     */
    @Override
    public void run(String... args) {
        HttpHeaders httpHeaders = new HttpHeaders();
        GetInfoRequestDto request = new GetInfoRequestDto();
        request.setSsn("123456789");
        GetInfoResponseDto response;
        try {
            response = customServerClient.getInfo(request, httpHeaders);
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

        request.setSsn(null);
        try {
            response = customServerClient.getInfo(request, httpHeaders);
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
            response = customServerClient.getInfo(request,httpHeaders);
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
            response = customServerClient.getInfo(request, httpHeaders);
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
            log.error("InternalServerError Exception:{}", e.toString());
        }
    }
}
