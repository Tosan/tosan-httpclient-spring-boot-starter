package com.tosan.client.http.sample.feignclient;

import com.tosan.client.http.core.circuitbreaker.CircuitBreaker;
import com.tosan.client.http.core.circuitbreaker.ExternalServiceProvider;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.InvalidParameterException;
import com.tosan.client.http.sample.server.api.exception.RequiredParameterException;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.ExternalServiceInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
@ExternalServiceProvider("custom-web-service2")
public class CircuitBreakerFeignClientService {

    private final ExternalServiceInvoker<CustomServerRestController> externalInvoker;

    @CircuitBreaker(provider = "callGetInfo")
    public GetInfoResponseDto callGetInfo(GetInfoRequestDto request, Map<String, String> headers)
            throws InvalidParameterException, RequiredParameterException {
        return externalInvoker.getClient().getInfo(request, headers);
    }

    @CircuitBreaker()
    public GetInfoResponseDto callErrorEndpoint() {
        return externalInvoker.getClient().error();
    }
}