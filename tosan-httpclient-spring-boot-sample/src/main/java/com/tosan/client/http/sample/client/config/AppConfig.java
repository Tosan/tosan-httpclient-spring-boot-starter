package com.tosan.client.http.sample.client.config;

import com.tosan.client.http.sample.server.api.config.properties.CustomServerClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    @ConfigurationProperties(prefix = "custom-server.client")
    public CustomServerClientConfig customServerClientConfig() {
        return new CustomServerClientConfig();
    }
}
