package com.tosan.client.http.sample.restclient;

import com.tosan.client.http.core.circuitbreaker.CircuitBreaker;
import com.tosan.client.http.restclient.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerRestClientService {

    private final ExternalServiceInvoker externalInvoker;

    @CircuitBreaker()
    public ResponseEntity<GetInfoResponseDto> callGetInfo(GetInfoRequestDto request) {
        log.info("Calling /custom-server/info with ssn=[{}]", request.getSsn());
        return externalInvoker
                .getClient()
                .post()
                .uri(externalInvoker.generateUrl("/custom-server/info"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(GetInfoResponseDto.class);
    }

    @CircuitBreaker()
    public ResponseEntity<GetInfoResponseDto> callErrorEndpoint() {
        log.info("Calling /custom-server/error (always returns 500)");
        return externalInvoker
                .getClient()
                .get()
                .uri(externalInvoker.generateUrl("/custom-server/error"))
                .retrieve()
                .toEntity(GetInfoResponseDto.class);
    }
}