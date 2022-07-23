package com.tosan.client.http.starter;

import com.tosan.client.http.starter.impl.feign.CustomErrorDecoder;
import com.tosan.client.http.starter.configuration.TosanFeignAutoConfiguration;
import feign.Feign;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class TosanFeignAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void customErrorDecoder() {
        this.contextRunner.withUserConfiguration(TosanFeignAutoConfiguration.class)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(CustomErrorDecoder.class);
                    assertThat(ctx.getBean(CustomErrorDecoder.class)).isInstanceOf(ErrorDecoder.class);
                });
    }

    @Test
    public void customErrorDecoder_missingClass() {
        this.contextRunner.withUserConfiguration(TosanFeignAutoConfiguration.class)
                .withClassLoader(new FilteredClassLoader(Feign.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(CustomErrorDecoder.class));
    }
}
