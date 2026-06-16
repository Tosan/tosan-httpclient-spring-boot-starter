package com.tosan.client.http.starter.configuration;

import com.tosan.client.http.core.Constants;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.ExceptionExtractType;
import com.tosan.client.http.starter.impl.feign.aspect.FeignUndeclaredThrowableExceptionAspect;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceException;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import com.tosan.tools.mask.starter.configuration.MaskBeanConfiguration;
import com.tosan.tools.mask.starter.replace.JacksonReplaceHelper;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import com.tosan.tools.mask.starter.replace.RegexReplaceHelper;
import feign.Feign;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

/**
 * AutoConfiguration for Feign
 *
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
@Configuration
@ConditionalOnClass({Feign.class})
public class TosanFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustomErrorDecoderConfig customErrorDecoderConfig() {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        customErrorDecoderConfig.addPackages("com.tosan");
        customErrorDecoderConfig.setExceptionExtractType(ExceptionExtractType.FULL_NAME_REFLECTION);
        customErrorDecoderConfig.setCheckedExceptionClass(TosanWebServiceException.class);
        customErrorDecoderConfig.setUncheckedExceptionClass(TosanWebServiceRuntimeException.class);
        return customErrorDecoderConfig;
    }

    @Bean
    public FeignUndeclaredThrowableExceptionAspect undeclaredThrowableExceptionAspect() {
        return new FeignUndeclaredThrowableExceptionAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder customErrorDecoder(CustomErrorDecoderConfig customErrorDecoderConfig) {
        return new CustomErrorDecoder(customErrorDecoderConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonReplaceHelperDecider replaceHelperDecider(
            JacksonReplaceHelper jacksonReplaceHelper, RegexReplaceHelper regexReplaceHelper,
            SecureParametersConfig secureParametersConfig) {
        return new JsonReplaceHelperDecider(jacksonReplaceHelper, regexReplaceHelper, secureParametersConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecureParametersConfig secureParametersConfig() {
        HashSet<SecureParameter> securedParameters = new HashSet<>(MaskBeanConfiguration.SECURED_PARAMETERS);
        securedParameters.add(Constants.AUTHORIZATION_SECURE_PARAM);
        securedParameters.add(Constants.PROXY_AUTHORIZATION_SECURE_PARAM);
        return new SecureParametersConfig(securedParameters);
    }
}
