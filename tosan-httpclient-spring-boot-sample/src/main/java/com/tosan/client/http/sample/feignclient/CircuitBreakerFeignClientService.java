package com.tosan.client.http.sample.feignclient;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.circuitbreaker.CircuitBreaker;
import com.tosan.client.http.core.service.ExternalService;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.InvalidParameterException;
import com.tosan.client.http.sample.server.api.exception.RequiredParameterException;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerFeignClientService {

    private final ExternalService<CustomServerRestController, HttpClientProperties> externalService;

    @CircuitBreaker()
    public GetInfoResponseDto callGetInfo(GetInfoRequestDto request, Map<String, String> headers)
            throws InvalidParameterException, RequiredParameterException {
        return externalService.getClient().getInfo(request, headers);
    }

    @CircuitBreaker()
    public GetInfoResponseDto callErrorEndpoint() {
        return externalService.getClient().error();
    }
}