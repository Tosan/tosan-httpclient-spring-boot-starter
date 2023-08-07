package com.tosan.client.http.sample.restclient;

import com.tosan.client.http.resttemplate.starter.impl.ExternalServiceInvoker;
import com.tosan.client.http.sample.restclient.exception.HttpClientRequestWrapperException;
import com.tosan.client.http.sample.server.api.config.feign.CustomServerFeignConfig;
import com.tosan.client.http.sample.server.api.model.GetInfoRequestDto;
import com.tosan.client.http.sample.server.api.model.GetInfoResponseDto;
import com.tosan.client.http.starter.impl.feign.exception.FeignClientRequestExecuteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.*;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.tosan.client.http.sample.restclient"}, exclude = CustomServerFeignConfig.class)
public class RestTemplateClientSpringBootApplication implements CommandLineRunner {

    @Autowired
    private ExternalServiceInvoker externalInvoker;

    public static void main(String[] args) {
        new SpringApplicationBuilder(RestTemplateClientSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .build()
                .run();
    }

    /**
     * First works fine
     * Second must throw InvalidParameterException
     * Third must throw RequiredParameterException
     * Forth must throw InternalServerError
     * Fifth must throw Unknown exception
     */
    @Override
    public void run(String... args) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        GetInfoRequestDto request = new GetInfoRequestDto();
        request.setSsn("123456789");
        ResponseEntity<GetInfoResponseDto> response;
        try {
            response = externalInvoker
                    .getRestTemplate().postForEntity(externalInvoker.generateUrl("/info"),
                            new HttpEntity<>(request, httpHeaders), GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        }  catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        try {
            response = externalInvoker.getRestTemplate()
                    .exchange(externalInvoker.generateUrl("/login"), HttpMethod.GET,
                            new HttpEntity<>(null, httpHeaders), GetInfoResponseDto.class);

            log.info("Response Info: {}", response);
        }  catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn(null);
        try {
            response = externalInvoker.getRestTemplate().postForEntity(externalInvoker.generateUrl("/info"),
                    new HttpEntity<>(request, httpHeaders), GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn("");
        try {
            response = externalInvoker.getRestTemplate().postForEntity(externalInvoker.generateUrl("/info"),
                    new HttpEntity<>(request, httpHeaders), GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("RestClient Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }

        request.setSsn("a1233");
        try {
            response = externalInvoker.getRestTemplate().postForEntity(externalInvoker.generateUrl("/info"),
                    new HttpEntity<>(request, httpHeaders), GetInfoResponseDto.class);
            log.info("Response Info: {}", response);
        } catch (HttpClientRequestWrapperException e) {
            log.error("HttpClientRequestWrapperException Info exception:", e);
        } catch (FeignClientRequestExecuteException e) {
            log.error("FeignClientRequestExecute Exception:", e);
        }
    }
}
