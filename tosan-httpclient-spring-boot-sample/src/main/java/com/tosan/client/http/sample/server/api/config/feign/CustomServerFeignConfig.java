package com.tosan.client.http.sample.server.api.config.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.sample.server.api.controller.CustomServerRestController;
import com.tosan.client.http.sample.server.api.exception.CustomServerException;
import com.tosan.client.http.sample.server.api.model.Context;
import com.tosan.client.http.starter.configuration.AbstractFeignConfiguration;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.ExceptionExtractType;
import com.tosan.client.http.starter.impl.feign.ExternalServiceInvoker;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.format.support.FormattingConversionService;

import java.util.List;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@Configuration
@Slf4j
@Import(FeignClientsConfiguration.class)
public class CustomServerFeignConfig extends AbstractFeignConfiguration<HttpClientProperties> {
    public static final String SERVICE_NAME = "custom-web-service2";

    public CustomServerFeignConfig(
            ObservationRegistry observationRegistry,
            JsonReplaceHelperDecider jacksonReplaceHelper,
            ObjectProvider<Feign.Builder> builderProvider,
            Encoder encoder,
            Decoder decoder,
            Contract contract) {
        super(SERVICE_NAME, HttpClientProperties.class, observationRegistry, jacksonReplaceHelper,
                builderProvider, encoder, decoder, contract);
    }

    @Bean(SERVICE_NAME)
    public ExternalServiceInvoker<CustomServerRestController> serviceInvokerBean(Environment environment) {
        return createServiceInvoker(environment, CustomServerRestController.PATH, CustomServerRestController.class);
    }

    @Override
    public CustomErrorDecoderConfig createCustomErrorDecoderConfig(ObjectMapper objectMapper) {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        customErrorDecoderConfig.getScanPackageList().add("com.tosan.client.http.sample.server.api.exception");
        customErrorDecoderConfig.setExceptionExtractType(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS);
        customErrorDecoderConfig.setCheckedExceptionClass(CustomServerException.class);
        customErrorDecoderConfig.setUncheckedExceptionClass(TosanWebServiceRuntimeException.class);
        customErrorDecoderConfig.setObjectMapper(objectMapper);
        return customErrorDecoderConfig;
    }

    @Override
    protected Contract createContract(ObjectMapper objectMapper) {
        FormattingConversionService conversionService = new FormattingConversionService();
        conversionService.addConverter(Context.class, String.class, source -> {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        });
        return new SpringMvcContract(List.of(), conversionService);
    }
}
