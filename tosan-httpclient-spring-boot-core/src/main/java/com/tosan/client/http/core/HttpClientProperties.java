package com.tosan.client.http.core;

import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
@Validated
public class HttpClientProperties {

    private String baseServiceUrl;
    @NestedConfigurationProperty
    private SSLConfiguration ssl = new SSLConfiguration();
    @NestedConfigurationProperty
    private ProxyConfiguration proxy = new ProxyConfiguration();
    @NestedConfigurationProperty
    private ConnectionConfiguration connection = new ConnectionConfiguration();
    @NestedConfigurationProperty
    private AuthorizationConfiguration authorization = new AuthorizationConfiguration();

    public String getBaseServiceUrl() {
        return baseServiceUrl;
    }

    public void setBaseServiceUrl(String baseServiceUrl) {
        this.baseServiceUrl = baseServiceUrl;
    }

    public SSLConfiguration getSsl() {
        return ssl;
    }

    public void setSsl(SSLConfiguration ssl) {
        this.ssl = ssl;
    }

    public ProxyConfiguration getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
    }

    public ConnectionConfiguration getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public AuthorizationConfiguration getAuthorization() {
        return authorization;
    }

    public void setAuthorization(AuthorizationConfiguration authorization) {
        this.authorization = authorization;
    }

    public static class ProxyConfiguration {
        private boolean enable;
        /**
         * hostname (IP or DNS name)
         */
        private String host;
        /**
         * port number
         */
        private String port;
        /**
         * proxy username
         */
        private String user;
        /**
         * proxy password
         */
        private String password;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class SSLConfiguration {
        private boolean enable;
        /**
         * Sets the SSLContext protocol algorithm name.
         */
        private String context = "TLSv1.2";
        private boolean checkValidity;
        @NestedConfigurationProperty
        private KeystoreConfiguration keystore = new KeystoreConfiguration();
        @NestedConfigurationProperty
        private TruststoreConfiguration truststore = new TruststoreConfiguration();

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public boolean isCheckValidity() {
            return checkValidity;
        }

        public void setCheckValidity(boolean checkValidity) {
            this.checkValidity = checkValidity;
        }

        public KeystoreConfiguration getKeystore() {
            return keystore;
        }

        public void setKeystore(KeystoreConfiguration keystore) {
            this.keystore = keystore;
        }

        public TruststoreConfiguration getTruststore() {
            return truststore;
        }

        public void setTruststore(TruststoreConfiguration truststore) {
            this.truststore = truststore;
        }
    }

    public static class KeystoreConfiguration {
        private String path;
        private String password;
        /**
         * the SSLkey store type. See the KeyStore section in the <a href=
         * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">Java
         * Cryptography Architecture Standard Algorithm Name Documentation</a> for more information.
         */
        private String type = "JKS";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class TruststoreConfiguration {
        private String path;
        private String password;
        /**
         * the SSLkey store type. See the KeyStore section in the <a href=
         * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">Java
         * Cryptography Architecture Standard Algorithm Name Documentation</a> for more information.
         */
        private String type = "JKS";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @Validated
    public static class ConnectionConfiguration {

        /**
         * Default value for max number of connections.
         */
        public static final int DEFAULT_MAX_CONNECTIONS = 200;

        /**
         * Default value for max number of connections per route.
         */
        public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 50;

        /**
         * Default value for time to live in milliseconds.
         */
        public static final long DEFAULT_TIME_TO_LIVE = 900000L;

        /**
         * Default value for following redirects.
         */
        public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

        /**
         * Default value for connection timeout in milliseconds.
         * A timeout value of zero is interpreted as an infinite timeout.
         * A negative value is interpreted as undefined (system default if applicable).
         */
        public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

        /**
         * Default value for connection timer repeat.
         */
        public static final int DEFAULT_CONNECTION_TIMER_REPEAT = 3000;

        /**
         * Default value for socket timeout in milliseconds.
         * A timeout value of zero is interpreted as an infinite timeout.
         * A negative value is interpreted as undefined (system default if applicable).
         */
        public static final int DEFAULT_SOCKET_TIMEOUT = 10000;

        private int maxConnections = DEFAULT_MAX_CONNECTIONS;

        private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;

        private long timeToLive = DEFAULT_TIME_TO_LIVE;

        private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;

        private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

        private int connectionTimerRepeat = DEFAULT_CONNECTION_TIMER_REPEAT;

        private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

        private String cookieSpecPolicy = StandardCookieSpec.IGNORE;


        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public int getMaxConnectionsPerRoute() {
            return maxConnectionsPerRoute;
        }

        public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        }

        public long getTimeToLive() {
            return timeToLive;
        }

        /**
         * @param timeToLive Defines the total span of time connections can be kept alive or execute requests in seconds.
         */
        public void setTimeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
        }

        public boolean isFollowRedirects() {
            return followRedirects;
        }

        public void setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getConnectionTimerRepeat() {
            return connectionTimerRepeat;
        }

        public void setConnectionTimerRepeat(int connectionTimerRepeat) {
            this.connectionTimerRepeat = connectionTimerRepeat;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public String getCookieSpecPolicy() {
            return cookieSpecPolicy;
        }

        public void setCookieSpecPolicy(String cookieSpecPolicy) {
            this.cookieSpecPolicy = cookieSpecPolicy;
        }
    }

    public static class AuthorizationConfiguration {
        private boolean enable;
        /**
         * authorization username
         */
        private String username;
        /**
         * authorization password
         */
        private String password;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
