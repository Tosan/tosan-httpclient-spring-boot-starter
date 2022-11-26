package com.tosan.client.http.core.certificate;

import com.tosan.client.http.core.HttpClientProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Loader for certificate (trust/keystore) related stuff
 *
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class CertificateLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateLoader.class);

    private CertificateLoader() {
    }

    /**
     * @param sslConfiguration    ssl configuration of http client
     * @param keyManagerFactory   configured {@link KeyManagerFactory} or JVMs default Key Managers
     * @param trustManagerFactory configured {@link TrustManagerFactory} or JVMs default Trust Managers
     * @return configured {@link SSLContext} or null
     */
    public static SSLContext buildSSLContext(HttpClientProperties.SSLConfiguration sslConfiguration,
                                             KeyManagerFactory keyManagerFactory,
                                             TrustManagerFactory trustManagerFactory) {
        try {
            SSLContext sslContext = SSLContext.getInstance(sslConfiguration.getContext());
            sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                    trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                    null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            LOGGER.error("Could not build SSLContext, skipping", ex);
        }
        return null;
    }

    public static SSLContext buildSSLContext(HttpClientProperties.SSLConfiguration sslConfiguration,
                                             KeyStore keyStore,
                                             KeyStore trustStore) {
        try {
            TrustStrategy trustStrategy = null;
            if (!sslConfiguration.isCheckValidity()) {
                trustStrategy = (chain, authType) -> true;
            }
            String password = sslConfiguration.getKeystore().getPassword();
            return SSLContexts.custom().setProtocol(sslConfiguration.getContext())
                    .loadKeyMaterial(keyStore, password != null ?
                            sslConfiguration.getKeystore().getPassword().toCharArray() : null)
                    .loadTrustMaterial(trustStore, trustStrategy).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException ex) {
            LOGGER.error("Could not build SSLContext, skipping", ex);
        }
        return null;
    }

    /**
     * @param sslConfiguration ssl configuration of http client
     * @return configured {@link TrustManagerFactory} or JVMs default Trust Managers
     */
    public static TrustManagerFactory getTrustManagerFactory(HttpClientProperties.SSLConfiguration sslConfiguration) {
        HttpClientProperties.TruststoreConfiguration truststoreConfiguration = sslConfiguration.getTruststore();
        if (StringUtils.isAnyBlank(truststoreConfiguration.getPath(), truststoreConfiguration.getPassword())) {
            LOGGER.warn("Truststore Configuration incomplete, skipping");
            return null;
        }

        try (FileInputStream is = new FileInputStream(ResourceUtils.getFile(truststoreConfiguration.getPath()))) {
            KeyStore keyStore = KeyStore.getInstance(truststoreConfiguration.getType());
            keyStore.load(is, truststoreConfiguration.getPassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            LOGGER.info("Truststore initialized successfully");
            return tmf;
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException ex) {
            LOGGER.error("Truststore could not be loaded, skipping", ex);
            return null;
        }
    }

    /**
     * @param sslConfiguration ssl configuration of http client
     * @return configured {@link KeyManagerFactory} or null
     */
    public static KeyManagerFactory getKeyManagerFactory(HttpClientProperties.SSLConfiguration sslConfiguration) {
        HttpClientProperties.KeystoreConfiguration keystoreConfiguration = sslConfiguration.getKeystore();
        if (StringUtils.isAnyBlank(keystoreConfiguration.getPath(), keystoreConfiguration.getPassword())) {
            LOGGER.warn("Keystore Configuration incomplete, skipping");
            return null;
        }
        try (FileInputStream is = new FileInputStream(ResourceUtils.getFile(keystoreConfiguration.getPath()))) {
            KeyStore keyStore = KeyStore.getInstance(keystoreConfiguration.getType());
            keyStore.load(is, keystoreConfiguration.getPassword().toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystoreConfiguration.getPassword().toCharArray());
            LOGGER.info("Keystore initialized successfully");
            return kmf;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException ex) {
            LOGGER.error("Keystore could not be loaded, skipping", ex);
            return null;
        }
    }

    public static KeyStore getKeyStore(HttpClientProperties.SSLConfiguration sslConfiguration) {
        HttpClientProperties.KeystoreConfiguration keystoreConfiguration = sslConfiguration.getKeystore();
        return getStore(keystoreConfiguration.getPath(), keystoreConfiguration.getPassword(), keystoreConfiguration.getType());
    }

    public static KeyStore getTrustStore(HttpClientProperties.SSLConfiguration sslConfiguration) {
        HttpClientProperties.TruststoreConfiguration truststoreConfiguration = sslConfiguration.getTruststore();
        return getStore(truststoreConfiguration.getPath(), truststoreConfiguration.getPassword(), truststoreConfiguration.getType());
    }

    private static KeyStore getStore(String path, String password, String type) {
        if (StringUtils.isAnyBlank(path, password)) {
            LOGGER.warn("Keystore Configuration incomplete, skipping");
            return null;
        }
        try (FileInputStream is = new FileInputStream(ResourceUtils.getFile(path))) {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(is, password.toCharArray());
            LOGGER.info("Keystore initialized successfully");
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            LOGGER.error("Keystore could not be loaded, skipping", ex);
            return null;
        }
    }
}
