package com.tosan.client.http.starter.impl.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tosan.client.http.starter.impl.feign.exception.*;
import com.tosan.client.http.starter.impl.feign.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
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
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            Response.Body body = response.body();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamUtils.copy(body.asInputStream(), output);
            String responseBody = new String(output.toByteArray());
            int status = response.status();
            LOGGER.info("ServerErrorResponse :\n ResponseStatus:{}\n ResponseBody:{}", status,
                    responseBody);
            Map<String, Class<? extends Exception>> exceptionMap = customErrorDecoderConfig.getExceptionMap();
            if (status >= 400 && status < 500) {
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
        } catch (Exception e) {
            LOGGER.warn("ServerInternalRuntimeException", e);
            InternalServerException internalServerException = new InternalServerException("Internal error", e);
            internalServerException.setErrorType("customErrorDecoder");
            internalServerException.setErrorCode(e.getClass().getSimpleName());
            Map<String, Object> exceptionErrorMap = new HashMap<>();
            exceptionErrorMap.put("localizedMessage", e.getLocalizedMessage());
            internalServerException.setErrorParam(exceptionErrorMap);
            internalServerException.setMessage(e.getMessage());
            return internalServerException;
        }
    }

    private <T> T jsonToObject(String string, Class<T> type) {
        try {
            return objectMapper.readValue(string, type);
        } catch (Exception e) {
            throw new JsonConvertException("error in converting Json to object");
        }
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
            Reflections reflections = new Reflections(scanPackageList.toArray());
            reflections.getSubTypesOf(checkedExceptionClass).forEach(this::extractAndFillMap);
            reflections.getSubTypesOf(uncheckedExceptionClass).forEach(this::extractAndFillMap);
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
                LOGGER.warn("error on construction of {}.", type.getSimpleName());
                LOGGER.warn("Exception:", e);
            }
        } else if (exceptionExtractType.equals(ExceptionExtractType.FULL_NAME_REFLECTION)) {
            exceptionMap.put(type.getName(), type);
        }
    }
}