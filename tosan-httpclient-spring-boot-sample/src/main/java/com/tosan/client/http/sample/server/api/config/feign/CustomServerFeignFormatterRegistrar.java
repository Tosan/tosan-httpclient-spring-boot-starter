package com.tosan.client.http.sample.server.api.config.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.sample.server.api.model.Context;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomServerFeignFormatterRegistrar implements FeignFormatterRegistrar {

    private final ObjectMapper objectMapper;

    public CustomServerFeignFormatterRegistrar(@Qualifier("customServer-objectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(Context.class, String.class, source -> {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}