package com.tosan.client.http.sample.feignclient.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Ali Alimohammadi
 * @since 12/28/2020
 */

@Configuration
@Order(1)
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
