package com.tosan.client.http.sample.server.api.controller;

import com.tosan.client.http.sample.server.api.exception.InvalidParameterException;
import com.tosan.client.http.sample.server.api.exception.RequiredParameterException;
import com.tosan.client.http.sample.server.api.model.Context;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.annotation.SdkController;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@SdkController
@FeignClient(contextId = "customServer-info")
public interface CustomServerRestController {
    String PATH = "/custom-server";

    @GetMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    GetInfoResponseDto login(@RequestHeader(name = "context") Context context);


    @PostMapping(value = "/info",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    GetInfoResponseDto getInfo(
            @RequestBody GetInfoRequestDto request, @RequestHeader HttpHeaders headers) throws InvalidParameterException, RequiredParameterException;
}
