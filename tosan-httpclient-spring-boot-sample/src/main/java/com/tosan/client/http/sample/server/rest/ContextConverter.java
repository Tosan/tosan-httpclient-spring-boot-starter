package com.tosan.client.http.sample.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.sample.server.api.model.Context;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContextConverter implements Converter<String, Context> {

    private final ObjectMapper objectMapper;

    @Override
    public Context convert(String contextString) {
        Context context;
        if (StringUtils.isEmpty(contextString)) {
            context = new Context();
        } else {
            try {
                context = objectMapper.readValue(contextString, Context.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            if (context == null) {
                context = new Context();
            }
        }
        return context;
    }
}