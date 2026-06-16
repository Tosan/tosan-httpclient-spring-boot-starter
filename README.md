# Spring Boot External HTTP Client Starters

This project provides two reusable Spring Boot starter modules for integrating external HTTP services. It offers
standard abstraction layers for both **Spring Cloud OpenFeign** and **Spring 6 `RestClient`**, both backed by a robust,
dynamically configured Apache HTTP Client.

## Shared Core Features

* **Dynamic Property Binding**: Standardized `application.yml` configuration (Connection pooling, SSL, Proxy, Basic
  Auth) mapped via Spring's `Binder`.
* **Apache HTTP Client Factory**: Centralized creation of `CloseableHttpClient` with advanced pooling (
  `PoolingHttpClientConnectionManagerBuilder`) and lifecycle management.
* **Observability**: Out-of-the-box integration with Micrometer `ObservationRegistry` for metrics and distributed
  tracing.
* **MDC & Headers**: Built-in interceptors for JSON content types, authentication, and context propagation (e.g.,
  `X-Request-Id`).
* **Template Method Pattern**: Abstract base classes allow developers to focus on service-specific logic while
  inheriting robust boilerplate.

---

## 1. Feign Client Starter

A module for creating declarative HTTP clients using Spring Cloud OpenFeign.

### Installation

```xml

<dependency>
    <groupId>com.tosan.client.http</groupId>
    <artifactId>tosan-httpclient-spring-boot-feignclient-starter</artifactId>
    <version>latest</version>
</dependency>
```

### Usage Guide

1. **Create the Feign Interface**:
   ```java
   public interface CustomServerRestController {
       String PATH = "/api/v1/resource";
       @GetMapping("/{id}")
       MyResourceDto getResource(@PathVariable("id") String id);
   }
   ```

2. **Extend `AbstractFeignConfiguration`**:
   *Note: Use `@Import(FeignClientsConfiguration.class)` to provide default OpenFeign beans.*
   ```java
   @Configuration
   @Import(FeignClientsConfiguration.class)
   public class CustomServerFeignConfig extends AbstractFeignConfiguration<HttpClientProperties> {
       
       public static final String SERVICE_NAME = "custom-web-service";

       public CustomServerFeignConfig(
               ObservationRegistry observationRegistry,
               JsonReplaceHelperDecider jacksonReplaceHelper,
               ObjectProvider<feign.Feign.Builder> builderProvider,
               Encoder encoder,
               Decoder decoder,
               Contract contract) {
           super(SERVICE_NAME, HttpClientProperties.class, observationRegistry, jacksonReplaceHelper,
                   builderProvider, encoder, decoder, contract);
       }

       @Bean(SERVICE_NAME)
       public ExternalServiceInvoker<CustomServerRestController> serviceInvokerBean(Environment environment) {
           return createServiceInvoker(environment, CustomServerRestController.PATH, CustomServerRestController.class);
       }

       @Override
       public CustomErrorDecoderConfig createCustomErrorDecoderConfig(ObjectMapper objectMapper) {
           CustomErrorDecoderConfig config = new CustomErrorDecoderConfig();
           config.getScanPackageList().add("com.example.api.exception");
           config.setExceptionExtractType(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS);
           config.setCheckedExceptionClass(CustomServerException.class);
           config.setUncheckedExceptionClass(CustomRuntimeException.class);
           config.setObjectMapper(objectMapper);
           return config;
       }
   }
   ```

3. **Inject and Use**:
   ```java
   @Service
   public class MyBusinessService {
       private final CustomServerRestController client;

       public MyBusinessService(@Qualifier(CustomServerFeignConfig.SERVICE_NAME) ExternalServiceInvoker<CustomServerRestController> invoker) {
           this.client = invoker.getServiceApi();
       }
   }
   ```

---

## 2. RestClient Starter

A module for creating fluent, modern HTTP clients using Spring Framework 6's `RestClient`.

### Installation

```xml

<dependency>
    <groupId>com.tosan.client.http</groupId>
    <artifactId>tosan-httpclient-spring-boot-restclient-starter</artifactId>
    <version>latest</version>
</dependency>
```

### Usage Guide

1. **Extend `AbstractRestClientConfiguration`**:
   ```java
   @Configuration
   public class CustomServerRestClientConfig extends AbstractRestClientConfiguration<HttpClientProperties> {

       public static final String SERVICE_NAME = "custom-web-service";

       public CustomServerRestClientConfig(
               ObservationRegistry observationRegistry,
               JsonReplaceHelperDecider jacksonReplaceHelper) {
           super(SERVICE_NAME, HttpClientProperties.class, observationRegistry, jacksonReplaceHelper);
       }

       @Bean(SERVICE_NAME)
       public RestClient customServerRestClient(Environment environment) {
           return super.getInstance(environment);
       }

       // Optional: Override hooks to customize behavior
       @Override
       protected void customizeRestClient(RestClient.Builder builder) {
           builder.defaultHeader("X-Custom-Header", "Value");
       }
       
       @Override
       protected List<ClientHttpRequestInterceptor> createInterceptors() {
           List<ClientHttpRequestInterceptor> interceptors = super.createInterceptors();
           // Add custom interceptors here
           return interceptors;
       }
   }
   ```

2. **Inject and Use**:
   ```java
   @Service
   public class MyBusinessService {
       private final RestClient restClient;

       public MyBusinessService(@Qualifier(CustomServerRestClientConfig.SERVICE_NAME) RestClient restClient) {
           this.restClient = restClient;
       }

       public MyResourceDto fetchData(String id) {
           return restClient.get()
                   .uri("/api/v1/resource/{id}", id)
                   .retrieve()
                   .body(MyResourceDto.class);
       }
   }
   ```

---

## Configuration Properties (application.yml)

Both modules utilize the same properties structure mapped via dynamic configuration binding. Define the properties in
your `application.yml` using the configured service name prefix (e.g., `[service-name].client`).
Both starters use the same class to configure settings.

| Config                                       | Description                          | Default   | Example                                | 
|----------------------------------------------|--------------------------------------|-----------|----------------------------------------|
| baseServiceUrl                               | Base service url (required)          |           | `localhost:8080/example`               |
| sslContext                                   | SSL Version (optional)               | `TLSv1.2` | `TLSv1.1`                              |
| connection.connectionTimeout (optional)      | Connection Timeout in ms             | 5000      | `2000`                                 |
| connection.socketTimeout (optional)          | Socket Timeout in ms                 | 10000     | `5000`                                 |
| connection.maxConnections (optional)         | max number of connections            | 200       | `100`                                  |
| connection.maxConnectionsPerRoute (optional) | max number of connections per route  | 50        | `10`                                   |
| connection.timeToLive (optional)             | time to live                         | 900       | `300`                                  |
| connection.timeToLiveUnit (optional)         | time to live unit                    | SECONDS   | `MINUTES`                              |
| connection.followRedirects (optional)        | following redirects                  | true      | `true` or `false`                      |
| connection.connectionTimerRepeat (optional)  | connection timer repeat              | 3000      | `1000`                                 |
| connection.cookieSpecPolicy (optional)       | Standard cookie specifications       | `ignore`  | `relaxed`,`strict`,`ignore`            |
| proxy (optional)                             | Configuration for used proxy servers |           |                                        |
| proxy.enable                                 | Proxy enable (optional)              | false     | `true` or `false`                      |
| proxy.host                                   | Hostname or IP of the Proxy          |           | `192.168.7.130` or `corp-proxy.domain` |
| proxy.port                                   | Port of the Proxy (optional)         |           | `100`                                  |
| proxy.user                                   | Proxy user name (optional)           |           | `testUser`                             |
| proxy.password                               | Proxy password (optional)            |           | `testPassword`                         |
| ssl (optional)                               | Configuration for ssl                |           |                                        |
| ssl.enable                                   | SSL Enable (optional)                | false     | `true` or `false`                      |
| ssl.context                                  | SSL Version (optional)               | `TLSv1.2` | `TLSv1.1`                              |
| ssl.check-validity                           | Check SSL validity (optional)        | false     | `true` or `false`                      |
| ssl.keystore.path                            | Keystore file path                   |           | `classpath:keystore.jks`               |
| ssl.keystore.password                        | Keystore password                    |           | `changeit`                             |
| ssl.keystore.type                            | Keystore type (optional)             | `JKS`     | `PKCS12`                               |
| ssl.truststore.path                          | Truststore file path                 |           | `classpath:truststore.jks`             |
| ssl.truststore.password                      | Truststore password                  |           | `changeit`                             |
| ssl.truststore.type                          | Truststore type (optional)           | `JKS`     | `PKCS12`                               |
| authorization.enable                         | Authorization enable (optional)      | false     | `true` or `false`                      |
| authorization.username                       | Authorization user name (optional)   |           | `testUser`                             |
| authorization.password                       | Authorization password (optional)    |           | `testPassword`                         |

Example:

```yaml
custom-web-service:
  client:
    base-service-url: "https://api.external-service.com"

    connection:
      max-connections: 200
      max-connections-per-route: 50
      time-to-live: 900000
      connection-timeout: 5000
      socket-timeout: 10000
      connection-timer-repeat: 3000
      follow-redirects: true

    authorization:
      enable: true
      username: "my-user"
      password: "my-password"

    proxy:
      enable: false
      host: "127.0.0.1"
      port: "8080"

    ssl:
      enable: true
      context: "TLSv1.2"
      check-validity: true
      keystore:
        path: "classpath:keystore.jks"
        password: "keystore-pass"
      truststore:
        path: "classpath:truststore.jks"
        password: "truststore-pass"
```