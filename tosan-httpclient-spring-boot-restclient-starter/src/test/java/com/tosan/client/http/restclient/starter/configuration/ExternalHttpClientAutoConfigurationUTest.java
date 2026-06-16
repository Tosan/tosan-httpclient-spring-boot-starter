package com.tosan.client.http.restclient.starter.configuration;

import com.tosan.client.http.restclient.starter.util.HttpLoggingInterceptorUtil;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import com.tosan.tools.mask.starter.configuration.MaskBeanConfiguration;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalHttpClientAutoConfigurationUTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void jsonReplaceHelperDecider() {
        this.contextRunner.withUserConfiguration(ExternalRestClientAutoConfiguration.class)
                .withUserConfiguration(MaskBeanConfiguration.class)
                .run(ctx -> assertThat(ctx).hasSingleBean(JsonReplaceHelperDecider.class));
    }

    @Test
    public void httpLoggingInterceptorUtil() {
        this.contextRunner.withUserConfiguration(ExternalRestClientAutoConfiguration.class)
                .withUserConfiguration(MaskBeanConfiguration.class)
                .run(ctx -> assertThat(ctx).hasSingleBean(HttpLoggingInterceptorUtil.class));
    }

    @Test
    public void secureParametersConfig() {
        this.contextRunner.withUserConfiguration(ExternalRestClientAutoConfiguration.class)
                .withUserConfiguration(MaskBeanConfiguration.class)
                .run(ctx -> assertThat(ctx).hasSingleBean(SecureParametersConfig.class));
    }
}
