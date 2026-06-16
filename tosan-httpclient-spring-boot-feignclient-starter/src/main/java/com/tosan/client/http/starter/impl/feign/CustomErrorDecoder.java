package com.tosan.client.http.starter.impl.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tosan.client.http.starter.impl.feign.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom error decoder for feign client
 *
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class CustomErrorDecoder implements ErrorDecoder, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final CustomErrorDecoderConfig customErrorDecoderConfig;
    private ObjectMapper objectMapper;

    public CustomErrorDecoder(CustomErrorDecoderConfig customErrorDecoderConfig) {
        this.customErrorDecoderConfig = customErrorDecoderConfig;
        this.objectMapper = customErrorDecoderConfig.getObjectMapper();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            Response.Body body = response.body();
            String responseBody = StreamUtils.copyToString(body.asInputStream(), StandardCharsets.UTF_8);
            int status = response.status();
            LOGGER.info("ServerErrorResponse :\n ResponseStatus:{}\n ResponseBody:{}", status,
                    responseBody);
            Map<String, Class<? extends Exception>> exceptionMap = customErrorDecoderConfig.getExceptionMap();
            if (status >= 400 && status < 500) {
                return extractBadRequestErrorException(responseBody, exceptionMap);
            }
            return extractInternalServerErrorException(responseBody, status, exceptionMap);
        } catch (Exception e) {
            LOGGER.error("ServerInternalRuntimeException", e);
            return extractCustomErrorDecoderException(e);
        }
    }

    protected Exception extractBadRequestErrorException(String responseBody,
                                                        Map<String, Class<? extends Exception>> exceptionMap) {
        ErrorObject errorObject = jsonToObject(responseBody, ErrorObject.class);
        String errorType = errorObject.getErrorType();
        String errorCode = errorObject.getErrorCode();
        String errorKey = errorType + "." + errorCode;
        Class<? extends Exception> exceptionClass = exceptionMap.get(errorKey);
        if (exceptionClass != null) {
            return jsonToObject(responseBody, exceptionClass);
        } else {
            UnknownException unknownException = jsonToObject(responseBody, UnknownException.class);
            unknownException.setJsonResponse(responseBody);
            return unknownException;
        }
    }

    protected Exception extractCustomErrorDecoderException(Exception e) {
        InternalServerException internalServerException = new InternalServerException("Internal error", e);
        internalServerException.setErrorType("customErrorDecoder");
        internalServerException.setErrorCode(e.getClass().getSimpleName());
        Map<String, Object> exceptionErrorMap = new HashMap<>();
        exceptionErrorMap.put("localizedMessage", e.getLocalizedMessage());
        internalServerException.setErrorParam(exceptionErrorMap);
        internalServerException.setMessage(e.getMessage());
        return internalServerException;
    }

    protected Exception extractInternalServerErrorException(String responseBody, int status,
                                                            Map<String, Class<? extends Exception>> exceptionMap) {
        InternalServerException internalServerException = jsonToObject(responseBody, InternalServerException.class);
        if (internalServerException.getErrorParam() == null) {
            Map<String, Object> errorMap = new HashMap<>();
            internalServerException.setErrorParam(errorMap);
        }
        String errorType = internalServerException.getErrorType();
        String errorCode = internalServerException.getErrorCode();
        String errorKey = errorType + "." + errorCode;
        Class<? extends Exception> cause = exceptionMap.get(errorKey);
        internalServerException.getErrorParam().put("httpStatusCode", status);
        internalServerException.getErrorParam().put("cause", cause);
        internalServerException.setJsonResponse(responseBody);
        return internalServerException;
    }

    @Override
    public void afterPropertiesSet() {
        ExceptionExtractType exceptionExtractType;
        if (customErrorDecoderConfig != null) {
            if (customErrorDecoderConfig.getObjectMapper() != null) {
                objectMapper = customErrorDecoderConfig.getObjectMapper();
            } else {
                objectMapper = getDefaultObjectMapper();
            }
            exceptionExtractType = customErrorDecoderConfig.getExceptionExtractType();
        } else {
            objectMapper = getDefaultObjectMapper();
            exceptionExtractType = ExceptionExtractType.STATIC_MAP;
        }

        if (exceptionExtractType != null && (exceptionExtractType.equals(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS) ||
                exceptionExtractType.equals(ExceptionExtractType.FULL_NAME_REFLECTION))) {
            List<String> scanPackageList = customErrorDecoderConfig.getScanPackageList();
            Class<? extends TosanWebServiceException> checkedExceptionClass =
                    customErrorDecoderConfig.getCheckedExceptionClass();
            Class<? extends TosanWebServiceRuntimeException> uncheckedExceptionClass =
                    customErrorDecoderConfig.getUncheckedExceptionClass();
            if (CollectionUtils.isEmpty(scanPackageList) || checkedExceptionClass == null || uncheckedExceptionClass == null) {
                throw new FeignConfigurationException(
                        "packageList and checkedExceptionClass and uncheckedExceptionClass be filled when " +
                                "extractType is EXCEPTION_IDENTIFIER_FIELDS or FULL_NAME_REFLECTION");
            }
            if (!Modifier.isAbstract(checkedExceptionClass.getModifiers())) {
                extractAndFillMap(checkedExceptionClass);
            }
            if (!Modifier.isAbstract(uncheckedExceptionClass.getModifiers())) {
                extractAndFillMap(uncheckedExceptionClass);
            }
            Reflections reflections = new Reflections(scanPackageList.toArray());
            reflections.getSubTypesOf(checkedExceptionClass)
                    .stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(this::extractAndFillMap);
            reflections.getSubTypesOf(uncheckedExceptionClass)
                    .stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(this::extractAndFillMap);
        }
    }

    protected <T> T jsonToObject(String string, Class<T> type) {
        try {
            return objectMapper.readValue(string, type);
        } catch (Exception e) {
            throw new JsonConvertException("error in converting Json to object", e);
        }
    }

    private ObjectMapper getDefaultObjectMapper() {
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        defaultObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return defaultObjectMapper;
    }

    private void extractAndFillMap(Class<? extends Exception> type) {
        ExceptionExtractType exceptionExtractType = customErrorDecoderConfig.getExceptionExtractType();
        Map<String, Class<? extends Exception>> exceptionMap = customErrorDecoderConfig.getExceptionMap();
        if (exceptionExtractType.equals(ExceptionExtractType.EXCEPTION_IDENTIFIER_FIELDS)) {
            try {
                TosanWebServiceBaseException exception = (TosanWebServiceBaseException) type
                        .getDeclaredConstructor().newInstance();
                String exceptionKey = exception.getErrorType() + "." + exception.getErrorCode();
                exceptionMap.put(exceptionKey, type);
            } catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
                     InvocationTargetException e) {
                LOGGER.error("error on construction of " + type.getSimpleName(), e);
            }
        } else if (exceptionExtractType.equals(ExceptionExtractType.FULL_NAME_REFLECTION)) {
            exceptionMap.put(type.getName(), type);
        }
    }
}