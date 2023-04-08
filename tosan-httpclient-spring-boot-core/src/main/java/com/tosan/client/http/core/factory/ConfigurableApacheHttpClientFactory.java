package com.tosan.client.http.core.factory;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.certificate.CertificateLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Factory used to create a HttpClient Instance
 *
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class ConfigurableApacheHttpClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableApacheHttpClientFactory.class);
    private final HttpClientProperties httpClientProperties;
    private final HttpClientBuilder httpClientBuilder;
    private final PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder;
    private final Timer connectionManagerTimer = new Timer("FeignApacheHttpClientConfiguration.connectionManagerTimer", true);

    public ConfigurableApacheHttpClientFactory(HttpClientBuilder httpClientBuilder,
                                               PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder,
                                               HttpClientProperties httpClientProperties) {
        this.httpClientBuilder = httpClientBuilder;
        this.httpClientProperties = httpClientProperties;
        this.connectionManagerBuilder = connectionManagerBuilder;
    }

    public HttpClientBuilder createBuilder() {
        configureTimeouts(httpClientBuilder);
        configureConnectionManager(httpClientBuilder);
        HttpClientProperties.ProxyConfiguration proxyConfig = httpClientProperties.getProxy();
        if (proxyConfig.isEnable()) {
            configureProxy(httpClientBuilder, proxyConfig);
            configureAuthentication(httpClientBuilder, proxyConfig);
        }

        return httpClientBuilder;
    }

    private void configureTimeouts(HttpClientBuilder builder) {
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(httpClientProperties.getConnection().getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(httpClientProperties.getConnection().getSocketTimeout(), TimeUnit.MILLISECONDS)
                .build());
    }

    private void configureConnectionManager(HttpClientBuilder builder) {
        TimeValue timeToLive = TimeValue.of(httpClientProperties.getConnection().getTimeToLive(),
                httpClientProperties.getConnection().getTimeToLiveUnit());
        connectionManagerBuilder
                .setMaxConnTotal(httpClientProperties.getConnection().getMaxConnections())
                .setMaxConnPerRoute(httpClientProperties.getConnection().getMaxConnectionsPerRoute())
                .setConnectionTimeToLive(timeToLive);
        String baseServiceUrl = httpClientProperties.getBaseServiceUrl();
        if (baseServiceUrl != null && baseServiceUrl.startsWith("https")) {
            configureSSL(connectionManagerBuilder);
        }
        PoolingHttpClientConnectionManager connectionManager = connectionManagerBuilder.build();
        this.connectionManagerTimer.schedule(new TimerTask() {
            public void run() {
                connectionManager.closeExpired();
            }
        }, 30000L, httpClientProperties.getConnection().getConnectionTimerRepeat());
        builder.setConnectionManager(connectionManager);
    }

    private void configureSSL(PoolingHttpClientConnectionManagerBuilder builder) {
        HttpClientProperties.SSLConfiguration sslConfiguration = httpClientProperties.getSsl();
        KeyStore trustStore = CertificateLoader.getTrustStore(sslConfiguration);
        KeyStore keyStore = CertificateLoader.getKeyStore(sslConfiguration);
        SSLContext sslContext = CertificateLoader.buildSSLContext(sslConfiguration, keyStore, trustStore);
        HostnameVerifier hostnameVerifier;
        if (sslConfiguration.isCheckValidity()) {
            hostnameVerifier = new DefaultHostnameVerifier();
        } else {
            LOGGER.warn("SSL validity check is disabled for url '{}'", httpClientProperties.getBaseServiceUrl());
            hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        }

        if (sslContext != null) {
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            builder.setSSLSocketFactory(sslSocketFactory);
        } else {
            LOGGER.warn("Invalid SSL Context, skipping");
        }
    }

    private void configureProxy(HttpClientBuilder builder, HttpClientProperties.ProxyConfiguration proxyConfig) {
        if (StringUtils.isNoneBlank(proxyConfig.getHost(), proxyConfig.getPort())) {
            builder.setProxy(new HttpHost(proxyConfig.getHost(), Integer.parseInt(proxyConfig.getPort())));
        } else {
            LOGGER.warn("Invalid Proxy Host, skipping");
        }
    }

    private void configureAuthentication(HttpClientBuilder builder, HttpClientProperties.ProxyConfiguration proxyConfig) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        boolean hasCredentials = false;
        if (StringUtils.isNoneBlank(proxyConfig.getUser(), proxyConfig.getPassword())) {
            credentialsProvider.setCredentials(
                    new AuthScope(proxyConfig.getHost(), Integer.parseInt(proxyConfig.getPort())),
                    new UsernamePasswordCredentials(proxyConfig.getUser(), proxyConfig.getPassword().toCharArray()));
            hasCredentials = true;
        }

        if (hasCredentials) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
            builder.setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy());
        }
    }
}
