package com.tosan.client.http.restclient.starter.configuration;

import com.tosan.client.http.core.Constants;
import com.tosan.client.http.restclient.starter.util.HttpLoggingInterceptorUtil;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import com.tosan.tools.mask.starter.configuration.MaskBeanConfiguration;
import com.tosan.tools.mask.starter.replace.JacksonReplaceHelper;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import com.tosan.tools.mask.starter.replace.RegexReplaceHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Configuration
public class ExternalRestClientAutoConfiguration {

    @Bean({"http-client-util-regex-replace-helper"})
    @ConditionalOnMissingBean(name = "http-client-util-regex-replace-helper")
    public JsonReplaceHelperDecider replaceHelperDecider(
            JacksonReplaceHelper jacksonReplaceHelper, RegexReplaceHelper regexReplaceHelper,
            @Qualifier("http-client-util-secured-parameters") SecureParametersConfig secureParametersConfig) {
        return new JsonReplaceHelperDecider(jacksonReplaceHelper, regexReplaceHelper, secureParametersConfig);
    }

    @Bean({"http-client-util-secured-parameters"})
    @ConditionalOnMissingBean(name = "http-client-util-secured-parameters")
    public SecureParametersConfig secureParametersConfig() {
        HashSet<SecureParameter> securedParameters = new HashSet<>(MaskBeanConfiguration.SECURED_PARAMETERS);
        securedParameters.add(Constants.AUTHORIZATION_SECURE_PARAM);
        securedParameters.add(Constants.PROXY_AUTHORIZATION_SECURE_PARAM);
        return new SecureParametersConfig(securedParameters);
    }

    @Bean
    public HttpLoggingInterceptorUtil httpLoggingInterceptorUtil(
            @Qualifier("http-client-util-regex-replace-helper") JsonReplaceHelperDecider replaceHelperDecider) {
        return new HttpLoggingInterceptorUtil(replaceHelperDecider);
    }
}
