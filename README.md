# tosan-httpclient-spring-boot-starter

This project provides two Spring-Boot Starter that enables the additional configuration of the used Httpclients and
FeignClient for produce sdk or rest template client.

## Usage
To use these starters, it is enough to add the following dependencies to the project based on your needs so that
the configuration are brought to the project, therefore you only need to add it as a maven dependency.
The `tosan-httpclient-spring-boot-starter` brings the required configuration for produce sdk in http server and the
`tosan-httpclient-spring-boot-resttemplate-starter` brings the required configuration for rest template client in every
consumer of any rest web service.
The usage of both are completely separate from each other.

```
        <dependency>
            <groupId>com.tosan.client.http</groupId>
            <artifactId>tosan-httpclient-spring-boot-starter</artifactId>
            <version>latest-version</version>
        </dependency>
```

```
        <dependency>
            <groupId>com.tosan.client.http</groupId>
            <artifactId>tosan-httpclient-spring-boot-resttemplate-starter</artifactId>
            <version>latest-version</version>
        </dependency>
```

### Feign

This project consists of two main parts, one of which is feign client.\
All configuration of feign client is described
in [feign-client](https://cloud.spring.io/spring-cloud-openfeign/reference/html/).


### Produce SDK
The main benefit of this starter is to help you for build SDK for Rest web services.
for this purpose, the actions mentioned in the following steps should be done.

> 1. Create Configuration class that inherit [AbstractFeignConfiguration](./tosan-httpclient-spring-boot-starter/src/main/java/com/tosan/client/http/starter/configuration/AbstractFeignConfiguration.java) class and override all methods.\
     All methods of this configuration class must be annotated with @Bean(\"[yourWebServiceName]-methodName\") and all input arg of these methods must be annotated with @Qualifier(\"[yourDefineBeanName]\")\
     See [CustomServerFeignConfig](./tosan-httpclient-spring-boot-sample/src/main/java/com/tosan/client/http/sample/server/api/config/feign/CustomServerFeignConfig.java)\
     The super method should be called in  all methods of this class except one abstract method([customErrorDecoderConfig](#custom_error_decoder_config)).
> 2. Define a bean for client config class inherit [HttpClientProperties](./tosan-httpclient-spring-boot-core/src/main/java/com/tosan/client/http/core/HttpClientProperties.java) class with a custom prefix for setting of project in this configuration class.
     see [Configuration](#configuration) section.
> 3. The errors returned by a service provider must follow the [Exception specific standard](#exception_specific_standard).
> 4. Define interfaces to provide api to your clients. In these interfaces, you should put the signature of your methods.
     All the annotations related to RequestMapping should be defined in these interfaces, and the controllers should inherit from them.
     All these interfaces must be annotated with @SdkController and @FeignClient.\
     If you have @RequestMapping on the these interface, define a string variable called PATH with value of RequestMapping annotation, then remove @RequestMapping in interface and add @RequestMapping(YourInterface.PATH) to concrete controller class.\
     See [CustomServerRestController](./tosan-httpclient-spring-boot-sample/src/main/java/com/tosan/client/http/sample/server/api/controller/CustomServerRestController.java)





<a name="exception_specific_standard">
</a>

### Exception specific standard
There are two Exception classes for create checked or unchecked exception.
([TosanWebServiceException](./tosan-httpclient-spring-boot-starter/src/main/java/com/tosan/client/http/starter/impl/feign/exception/TosanWebServiceException.java) and [TosanWebServiceRuntimeException](./tosan-httpclient-spring-boot-starter/src/main/java/com/tosan/client/http/starter/impl/feign/exception/TosanWebServiceRuntimeException.java))\
All api exceptions must be inherited one of them. all of them must be had a no arg constructor.


Note that the errors returned by a service provider must follow the following standard:

```
{
    "errorType": "validation",
    "errorCode": "InvalidParameterException",
    "message": "invalid user name",
    "errorParam": {
        "username": "1ali$!"
    }
}
```


Also, You can use [ErrorObject](./tosan-httpclient-spring-boot-starter/src/main/java/com/tosan/client/http/starter/impl/feign/ErrorObject.java) instead of making object on your own .



<a name="custom_error_decoder_config">
</a>

### Custom error decoder config definition
Now it is the turn of the only remaining abstract method in the configuration class.
This project provides configuration bean called customErrorDecoderConfig [CustomErrorDecoderConfig](./tosan-httpclient-spring-boot-starter/src/main/java/com/tosan/client/http/starter/impl/feign/CustomErrorDecoderConfig.java) to identify your errors.

```
    @Override
    @Bean("customServer-feignErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig(@Qualifier("customServer-objectMapper") ObjectMapper objectMapper) {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        customErrorDecoderConfig.getScanPackageList().add("com.tosan.client.sample.server.api.exception");
        customErrorDecoderConfig.setExceptionExtractType(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS);
        customErrorDecoderConfig.setCheckedExceptionClass(CustomServerException.class);
        customErrorDecoderConfig.setUncheckedExceptionClass(TosanWebServiceRuntimeException.class);
        customErrorDecoderConfig.setObjectMapper(objectMapper);
        return customErrorDecoderConfig;
    }
```


### Exception extract type



The types of ExceptionExtractType include the following:

> * STATIC_MAP
> * EXCEPTION_IDENTIFIER_FIELDS
> * FULL_NAME_REFLECTION

#### set ExceptionExtractType to define creation strategy of conversion map.

```
    @Bean("customServer-customErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig() {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        ...
        customErrorDecoderConfig.setExceptionExtractType(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS);
        ...
        return customErrorDecoderConfig;
    }
```

### STATIC_MAP type strategy
in this strategy you must make exceptionMap manually.

        `key = errorType.errorCode`

        `value = ExceptionName.class`

```
        private static Map<String, Class<? extends Exception>> exceptionMap = new HashMap<>();
            static {
                    exceptionMap.put("validation.InvalidParameterException", InvalidParameterException.class);
            } 
```

### EXCEPTION_IDENTIFIER_FIELDS type strategy

At first You must define exceptions that inherit TosanWebServiceException and TosanWebServiceRuntimeException classes.
these classes have two methods(getErrorType() and getErrorCode()).
then add exception packages to scan automatically and detect sub type of TosanWebServiceException and TosanWebServiceRuntimeException classes.
it will make conversion map automatically by these methods and simple name of exception classes.

        `key = getErrorType() + '.' + getErrorCode()`

        `value = ExceptionName.class`

### FULL_NAME_REFLECTION type strategy

At first You must define exceptions that inherit TosanWebServiceException and TosanWebServiceRuntimeException classes.
then add exception packages to scan automatically and detect sub type of TosanWebServiceException and TosanWebServiceRuntimeException classes.
it will make conversion map automatically by full package name of exception classes.

        `key = ExceptionName.class.getSimpleName()`

        `value = ExceptionName.class`

**_NOTE:_** Add exception packages to scan automatically in FULL_NAME_REFLECTION and EXCEPTION_IDENTIFIER_FIELDS strategy

```
    @Bean("customServer-customErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig() {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        ...
        customErrorDecoderConfig.getScanPackageList().add(CustomServerException.class.getPackageName());
        ...
        return customErrorDecoderConfig;
    }
```
**_NOTE:_**
Set CheckedExceptionClass and UncheckedExceptionClass in FULL_NAME_REFLECTION and EXCEPTION_IDENTIFIER_FIELDS strategy

```
    @Bean("customServer-customErrorDecoderConfig")
    public CustomErrorDecoderConfig customErrorDecoderConfig() {
        CustomErrorDecoderConfig customErrorDecoderConfig = new CustomErrorDecoderConfig();
        ...
        customErrorDecoderConfig.setCheckedExceptionClass(CustomServerException.class);
        customErrorDecoderConfig.setUncheckedExceptionClass(CustomServerRuntimeException.class);
        ...
        return customErrorDecoderConfig;
    }
```

If an error key not defined in the map and http status code is 4xx, error decoder returns `UnknownException`.It has
another parameter besides standard parameters called jsonResponse that is response body of the error.

This conversion just do for http status that are equals or greater than 400 and less than 500. It
returns `InternalServerErrorException` for other http status error code.


### Rest template starter
The main benefit of this starter is to help you for consume any Rest web services.
for this purpose, the actions mentioned in the following steps should be done.

> 1. Create Configuration class that inherit [AbstractHttpClientConfiguration](./tosan-httpclient-spring-boot-resttemplate-starter/src/main/java/com/tosan/client/http/resttemplate/starter/configuration/AbstractHttpClientConfiguration.java) class and override all methods.\
     All methods of this configuration class must be annotated with @Bean(\"[yourWebServiceName]-methodName\") and all input arg of these methods must be annotated with @Qualifier(\"[yourDefineBeanName]\")\
     See [ExternalServiceConfiguration](./tosan-httpclient-spring-boot-sample/src/main/java/com/tosan/client/http/sample/restclient/config/ExternalServiceConfiguration.java)\
     The super method should be called in  all methods of this class except three abstract method([responseErrorHandler](#response_error_handler),getExternalServiceName,[clientConfig](#configuration)).
> 2. Implement getExternalWebService by returning web service name.
> 3. Define a bean for client config class inherit [HttpClientProperties](./tosan-httpclient-spring-boot-core/src/main/java/com/tosan/client/http/core/HttpClientProperties.java) class with a custom prefix for setting of project in this configuration class.
     see [Configuration](#configuration) section.
> 4. For handle error of service must follow the [Error Handler Interceptor](#error_handler_interceptor).
> 5. At last inject ExternalServiceInvoker bean in app and get rest template and call any service.


## Configuration

<a name="configuration">
</a>
Both starters use the same class to configure settings.
You can define client config class custom prefix for setting that inherit HttpClientProperties class.
All configuration values are prefixed by your defined prefix. (e.g. prefix=custom-service.client  `custom-service.client.connection.connectionTimeout`).


| Config | Description | Default | Example | 
|---|---|---|---|
| baseServiceUrl | Base service url (required) | | `localhost:8080/example` |
| sslContext | SSL Version (optional) | `TLSv1.2` | `TLSv1.1` |
| connection.connectionTimeout (optional) | Connection Timeout in ms | 5000 | `2000` |
| connection.socketTimeout (optional) |  Socket Timeout in ms | 10000 | `5000` |
| connection.maxConnections (optional) |  max number of connections | 200 | `100` |
| connection.maxConnectionsPerRoute (optional) |  max number of connections per route | 50 | `10` |
| connection.timeToLive (optional) |  time to live | 900 | `300` |
| connection.timeToLiveUnit (optional) |  time to live unit | SECONDS | `MINUTES` |
| connection.followRedirects (optional) |  following redirects | true | `true` or `false` |
| connection.connectionTimerRepeat (optional) |  connection timer repeat | 3000 | `1000` |
| proxy (optional) | Configuration for used proxy servers | | |
| proxy.enable | Proxy enable (optional) | false | `true` or `false` |
| proxy.host | Hostname or IP of the Proxy | | `192.168.7.130` or `corp-proxy.domain` |
| proxy.port | Port of the Proxy (optional) | | `100` |
| proxy.user | Proxy user name (optional) | | `testUser`|
| proxy.password | Proxy password (optional) | | `testPassword` |
| ssl (optional) | Configuration for ssl | | |
| ssl.context | SSL Version (optional) | `TLSv1.2` | `TLSv1.1` |
| ssl.check-validity | Check SSL validity (optional) | false | `true` or `false` |
| ssl.keystore.path | Keystore file path | | `classpath:keystore.jks` |
| ssl.keystore.password | Keystore password | | `changeit` |
| ssl.keystore.type | Keystore type (optional) | `JKS` | `PKCS12` |
| ssl.truststore.path | Truststore file path | | `classpath:truststore.jks` |
| ssl.truststore.password | Truststore password | | `changeit` |
| ssl.truststore.type | Truststore type (optional) | `JKS` | `PKCS12` |
| authorization.enable | Authorization enable (optional) | false | `true` or `false` |
| authorization.username | Authorization user name (optional) | | `testUser`|
| authorization.password | Authorization password (optional) | | `testPassword` |

Example:

```
custom-service:
      client:
        proxy:
            host: localhost
            port: 3333
            user: testUser
            password: testPassword
    
        connection:
          connectionTimeout: 5000
          socketTimeout: 10000
```



```
    public class CustomServerClientConfig extends HttpClientProperties {
    }
```

```
    @Bean
    @ConfigurationProperties(prefix = "custom-service")
    @ConditionalOnMissingBean
    public HttpClientProperties customServerClientConfig() {
        return new CustomServerClientConfig();
    }
```

## Sample Project

You can find a sample project which configure `Feign` to use `Apache HttpClient`
in tosan-httpclient-spring-boot-sample
