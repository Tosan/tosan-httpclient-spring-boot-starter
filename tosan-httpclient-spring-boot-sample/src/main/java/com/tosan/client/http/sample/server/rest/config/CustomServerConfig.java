package com.tosan.client.http.sample.server.rest.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomServerConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return JsonMapper.builder().build();
    }
}
