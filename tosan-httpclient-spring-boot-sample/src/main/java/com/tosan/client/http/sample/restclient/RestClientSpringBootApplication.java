package com.tosan.client.http.sample.restclient;

import com.tosan.client.http.restclient.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.sample.restclient.exception.HttpClientRequestWrapperException;
import com.tosan.client.http.sample.server.api.config.feign.CustomServerFeignConfig;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.exception.FeignClientRequestExecuteException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.tosan.client.http.sample.restclient"}, exclude = CustomServerFeignConfig.class)
@RequiredArgsConstructor
public class RestClientSpringBootApplication implements CommandLineRunner {

    private final ExternalServiceInvoker externalInvoker;
    private final CircuitBreakerRestClientService circuitBreakerRestClientService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(RestClientSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .build()
                .run();
    }

    /**
     * First works fine
     * Second must throw MissingRequestHeaderException
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

        GetInfoRequestDto request = new GetInfoRequestDto();
        request.setSsn("123456789");
        ResponseEntity<GetInfoResponseDto> response;
        try {
            response = externalInvoker
                    .getClient().post().uri(externalInvoker.generateUrl("/custom-server/info"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve().toEntity(GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn(null);
        try {
            response = externalInvoker
                    .getClient()
                    .post()
                    .uri(externalInvoker.generateUrl("/custom-server/info"))
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().toEntity(GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn("");
        try {
            response = externalInvoker
                    .getClient()
                    .post()
                    .uri(externalInvoker.generateUrl("/custom-server/info"))
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().toEntity(GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("RestClient Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn("a1233");
        try {
            response = externalInvoker
                    .getClient()
                    .post()
                    .uri(externalInvoker.generateUrl("/custom-server/info")).body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().toEntity(GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        GetInfoRequestDto validRequest = new GetInfoRequestDto();
        validRequest.setSsn("123456789");
        try {
            response = circuitBreakerRestClientService.callGetInfo(validRequest);
            log.info("[CB] Successful call -> Response: {}", response);
        } catch (Exception e) {
            log.error("[CB] Unexpected exception on successful call:", e);
        }

        for (int i = 1; i <= 4; i++) {
            try {
                circuitBreakerRestClientService.callErrorEndpoint();
            } catch (CallNotPermittedException e) {
                log.warn("[CB] Call {} rejected because circuit breaker is OPEN (CallNotPermittedException)", i);
            } catch (Exception e) {
                log.warn("[CB] Call {} recorded as failure: {}", i, e.getClass().getSimpleName());
            }
        }

        try {
            response = circuitBreakerRestClientService.callGetInfo(validRequest);
            log.info("[CB] Response: {}", response);
        } catch (CallNotPermittedException e) {
            log.warn("[CB] Valid call rejected while circuit breaker is OPEN -> {}", e.getMessage());
        } catch (Exception e) {
            log.error("[CB] Unexpected exception:", e);
        }

    }
}