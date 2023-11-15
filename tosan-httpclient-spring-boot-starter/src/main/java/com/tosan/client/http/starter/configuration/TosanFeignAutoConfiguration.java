package com.tosan.client.http.starter.configuration;

import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.impl.feign.CustomErrorDecoderConfig;
import com.tosan.client.http.starter.impl.feign.ExceptionExtractType;
import com.tosan.client.http.starter.impl.feign.aspect.FeignUndeclaredThrowableExceptionAspect;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceException;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;
import com.tosan.client.http.starter.util.KeyValueReplaceHelper;
import com.tosan.tools.mask.starter.business.ComparisonTypeFactory;
import com.tosan.tools.mask.starter.business.ValueMaskFactory;
import feign.Feign;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AutoConfiguration for the Feign
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
    public KeyValueReplaceHelper keyValueReplaceHelper(ValueMaskFactory valueMaskFactory,
                                                       ComparisonTypeFactory comparisonTypeFactory) {
        return new KeyValueReplaceHelper(valueMaskFactory,comparisonTypeFactory);
    }
}
