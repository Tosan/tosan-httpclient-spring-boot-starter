package com.tosan.client.http.sample.server.rest;

import com.tosan.client.http.sample.server.api.config.feign.CustomServerFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@SpringBootApplication(scanBasePackages = "com.tosan.client.http.sample.server.rest", exclude = CustomServerFeignConfig.class)
public class RestServerSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestServerSpringBootApplication.class, args);
    }
}
