package com.tosan.client.http.sample.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.core.Constants;
import com.tosan.tools.mask.starter.business.enumeration.MaskType;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashSet;

import static com.tosan.tools.mask.starter.configuration.MaskBeanConfiguration.SECURED_PARAMETERS;

/**
 * @author Ali Alimohammadi
 * @since 12/28/2020
 */

@Configuration
@Order(1)
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean({"http-client-util-secured-parameters"})
    public SecureParametersConfig secureParametersConfig() {
        HashSet<SecureParameter> securedParameters = new HashSet<>(SECURED_PARAMETERS);
        securedParameters.add(Constants.AUTHORIZATION_SECURE_PARAM);
        securedParameters.add(Constants.PROXY_AUTHORIZATION_SECURE_PARAM);
        securedParameters.add(new SecureParameter("password", MaskType.COMPLETE));
        return new SecureParametersConfig(securedParameters);
    }
}
