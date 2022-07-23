package com.tosan.client.http.starter;

import com.tosan.client.http.starter.impl.ConfigurableApacheHttpClientFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigurableApacheHttpClientFactoryTest {

    private final HttpClientProperties.ConnectionConfiguration connectionConfiguration = new HttpClientProperties.ConnectionConfiguration();
    private final HttpClientProperties.SSLConfiguration sslConfiguration = new HttpClientProperties.SSLConfiguration();
    private final HttpClientProperties.ProxyConfiguration proxyConfiguration = new HttpClientProperties.ProxyConfiguration();
    @Mock
    private HttpClientProperties httpClientProperties;
    private HttpClientProperties.ProxyConfiguration hostConfig;
    private HttpClientProperties.ProxyConfiguration hostConfigWithAuth;

    @BeforeEach
    public void setup() {
        when(httpClientProperties.getProxy()).thenReturn(proxyConfiguration);
        when(httpClientProperties.getConnection()).thenReturn(connectionConfiguration);
        when(httpClientProperties.getSsl()).thenReturn(sslConfiguration);

        hostConfig = new HttpClientProperties.ProxyConfiguration();
        hostConfigWithAuth = new HttpClientProperties.ProxyConfiguration();
        hostConfigWithAuth.setEnable(true);
        hostConfigWithAuth.setHost("testProxyHost");
        hostConfigWithAuth.setPort("1234");
        hostConfigWithAuth.setUser("testUser");
        hostConfigWithAuth.setPassword("testPassword");
    }

    @Test
    public void createBuilder_defaultConfiguration() {
        ConfigurableApacheHttpClientFactory underTest = new ConfigurableApacheHttpClientFactory(HttpClientBuilder.create(),
                new DefaultApacheHttpClientConnectionManagerFactory(), httpClientProperties);
        HttpClientBuilder builder = underTest.createBuilder();
        RequestConfig requestConfig = (RequestConfig) ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "defaultRequestConfig");
        assertThat(requestConfig).isNotNull();
        assertThat(requestConfig.getConnectTimeout()).isEqualTo(HttpClientProperties.ConnectionConfiguration.DEFAULT_CONNECTION_TIMEOUT);
        assertThat(requestConfig.getSocketTimeout()).isEqualTo(HttpClientProperties.ConnectionConfiguration.DEFAULT_SOCKET_TIMEOUT);

        HttpRoutePlanner proxySelector = (SystemDefaultRoutePlanner) ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "routePlanner");
        assertThat(proxySelector).isNull();
    }

    @Test
    public void createBuilder_timeoutConfiguration() {
        connectionConfiguration.setConnectionTimeout(1234);
        connectionConfiguration.setSocketTimeout(5678);
        ConfigurableApacheHttpClientFactory underTest = new ConfigurableApacheHttpClientFactory(HttpClientBuilder.create(),
                new DefaultApacheHttpClientConnectionManagerFactory(), httpClientProperties);
        HttpClientBuilder builder = underTest.createBuilder();

        RequestConfig requestConfig = (RequestConfig) ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "defaultRequestConfig");
        assertThat(requestConfig.getConnectTimeout()).isEqualTo(1234);
        assertThat(requestConfig.getSocketTimeout()).isEqualTo(5678);
    }

    @Test
    public void createBuilder_proxyConfiguration_noAuthentication() {
        when(httpClientProperties.getProxy()).thenReturn(hostConfig);

        ConfigurableApacheHttpClientFactory underTest = new ConfigurableApacheHttpClientFactory(HttpClientBuilder.create(),
                new DefaultApacheHttpClientConnectionManagerFactory(), httpClientProperties);
        HttpClientBuilder builder = underTest.createBuilder();

        Object credentialsProvider = ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "credentialsProvider");
        Object proxyAuthStrategy = ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "proxyAuthStrategy");
        assertThat(credentialsProvider).isNull();
        assertThat(proxyAuthStrategy).isNull();
    }

    @Test
    public void createBuilder_proxyConfiguration_authentication() {
        when(httpClientProperties.getProxy()).thenReturn(hostConfigWithAuth);

        ConfigurableApacheHttpClientFactory underTest = new ConfigurableApacheHttpClientFactory(HttpClientBuilder.create(),
                new DefaultApacheHttpClientConnectionManagerFactory(), httpClientProperties);
        HttpClientBuilder builder = underTest.createBuilder();

        BasicCredentialsProvider credentialsProvider = (BasicCredentialsProvider) ReflectionTestUtils
                .getField(builder, HttpClientBuilder.class, "credentialsProvider");
        assertThat(credentialsProvider).isNotNull();

        Credentials credentials = credentialsProvider.getCredentials(new AuthScope(hostConfigWithAuth.getHost(), Integer.parseInt(hostConfigWithAuth.getPort())));
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUserPrincipal().getName()).isEqualTo(hostConfigWithAuth.getUser());
        assertThat(credentials.getPassword()).isEqualTo(hostConfigWithAuth.getPassword());

        Object proxyAuthStrategy = ReflectionTestUtils.getField(builder, HttpClientBuilder.class, "proxyAuthStrategy");
        assertThat(proxyAuthStrategy).isInstanceOf(ProxyAuthenticationStrategy.class);
    }
}
