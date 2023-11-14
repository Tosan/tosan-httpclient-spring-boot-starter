package com.tosan.client.http.core.factory;

import com.tosan.client.http.core.HttpClientProperties;
import com.tosan.client.http.core.certificate.CertificateLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Factory used to create a HttpClient Instance
 *
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class ConfigurableApacheHttpClientFactory extends DefaultApacheHttpClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableApacheHttpClientFactory.class);
    private final HttpClientProperties httpClientProperties;
    private final ApacheHttpClientConnectionManagerFactory clientConnectionManagerFactory;
    private final Timer connectionManagerTimer = new Timer("FeignApacheHttpClientConfiguration.connectionManagerTimer", true);

    public ConfigurableApacheHttpClientFactory(HttpClientBuilder builder,
                                               ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
                                               HttpClientProperties httpClientProperties) {
        super(builder);
        this.httpClientProperties = httpClientProperties;
        this.clientConnectionManagerFactory = connectionManagerFactory;
    }

    @Override
    public HttpClientBuilder createBuilder() {
        HttpClientBuilder builder = super.createBuilder();
        configureTimeouts(builder);
        configureConnectionManager(builder);
        String baseServiceUrl = httpClientProperties.getBaseServiceUrl();
        if (baseServiceUrl != null && baseServiceUrl.startsWith("https")) {
            configureSSL(builder);
        }
        HttpClientProperties.ProxyConfiguration proxyConfig = httpClientProperties.getProxy();
        if (proxyConfig.isEnable()) {
            configureProxy(builder, proxyConfig);
            configureAuthentication(builder, proxyConfig);
        }

        return builder;
    }

    private void configureTimeouts(HttpClientBuilder builder) {
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(httpClientProperties.getConnection().getConnectionTimeout())
                .setSocketTimeout(httpClientProperties.getConnection().getSocketTimeout())
                .setRedirectsEnabled(httpClientProperties.getConnection().isFollowRedirects())
                .setCookieSpec(httpClientProperties.getConnection().getCookieSpecPolicy())
                .build());
    }

    private void configureConnectionManager(HttpClientBuilder builder) {
        final HttpClientConnectionManager connectionManager = clientConnectionManagerFactory
                .newConnectionManager(!httpClientProperties.getSsl().isCheckValidity(),
                        httpClientProperties.getConnection().getMaxConnections(),
                        httpClientProperties.getConnection().getMaxConnectionsPerRoute(),
                        httpClientProperties.getConnection().getTimeToLive(),
                        httpClientProperties.getConnection().getTimeToLiveUnit(), null);
        this.connectionManagerTimer.schedule(new TimerTask() {
            public void run() {
                connectionManager.closeExpiredConnections();
            }
        }, 30000L, (long) httpClientProperties.getConnection().getConnectionTimerRepeat());

        builder.setConnectionManager(connectionManager);
    }

    private void configureSSL(HttpClientBuilder builder) {
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
            builder.setSSLSocketFactory(sslSocketFactory).build();
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
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        boolean hasCredentials = false;
        if (StringUtils.isNoneBlank(proxyConfig.getUser(), proxyConfig.getPassword())) {
            credentialsProvider.setCredentials(
                    new AuthScope(proxyConfig.getHost(), Integer.parseInt(proxyConfig.getPort())),
                    new UsernamePasswordCredentials(proxyConfig.getUser(), proxyConfig.getPassword()));
            hasCredentials = true;
        }

        if (hasCredentials) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
            builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        }
    }
}
