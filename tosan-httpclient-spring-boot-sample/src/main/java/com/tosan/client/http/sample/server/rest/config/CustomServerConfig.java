package com.tosan.client.http.sample.server.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomServerConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
