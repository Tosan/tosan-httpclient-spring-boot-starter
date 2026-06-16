package com.tosan.client.http.starter.impl.feign.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.RawValue;
import com.tosan.tools.mask.starter.dto.JsonReplaceResultDto;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import feign.Request;
import feign.Response;
import feign.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static feign.Util.UTF_8;

/**
 * @author Ali Alimohammadi
 * @since 8/7/2023
 */
public class HttpFeignClientLogger extends feign.Logger {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(HttpFeignClientLogger.class);

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final String webServiceName;
    private final JsonReplaceHelperDecider replaceHelperDecider;

    public HttpFeignClientLogger(String webServiceName, JsonReplaceHelperDecider replaceHelperDecider) {
        this.webServiceName = webServiceName;
        this.replaceHelperDecider = replaceHelperDecider;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logger.isInfoEnabled()) {
            final Map<String, Object> requestData = new LinkedHashMap<>();
            requestData.put("invoke", webServiceName);
            String urlString = request.url();
            boolean hasQueryString = urlString.contains("?");
            if (hasQueryString) {
                String[] splitUrl = urlString.split("[?]");
                String maskedQueryString = splitUrl.length > 1 ? getMaskedQueryString(splitUrl[1]) : "";
                requestData.put("service", getServiceName(methodTag(configKey)));
                requestData.put("url", request.httpMethod() + " " + splitUrl[0] + "?" + maskedQueryString);
            } else {
                requestData.put("service", getServiceName(methodTag(configKey)));
                requestData.put("url", request.httpMethod() + " " + urlString);
            }

            if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
                if (!request.headers().isEmpty()) {
                    requestData.put("headers", getMaskedHeaders(request.headers()));
                }
                if (logLevel.ordinal() >= Level.FULL.ordinal()) {
                    byte[] byteArrayBody = request.body();
                    if (byteArrayBody != null) {
                        String maskedBody = replaceHelperDecider.replace(new String(byteArrayBody, StandardCharsets.UTF_8));
                        requestData.put("body", new RawValue(maskedBody));
                    }
                }
            }
            logger.info(toJson(requestData));
        }
    }

    @Override
    protected void log(String configKey, String format, Object... args) {

    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
        if (logger.isInfoEnabled()) {
            final Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("invoked", webServiceName);
            int status = response.status();
            responseData.put("service", getServiceName(methodTag(configKey)));
            responseData.put("duration", elapsedTime / 1000.0 + "s");
            responseData.put("status", status);
            if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
                if (!response.headers().isEmpty()) {
                    responseData.put("headers", getMaskedHeaders(response.headers()));
                }
                if (logLevel.ordinal() >= Level.FULL.ordinal()) {
                    if (response.body() != null && !(status == 204 || status == 205)) {
                        byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                        if (bodyData != null && bodyData.length > 0) {
                            String securedBody = decodeOrDefault(bodyData, UTF_8, "Binary data");
                            responseData.put("body", new RawValue(securedBody));
                        }
                        response = response.toBuilder().body(bodyData).build();
                    }
                }
            }
            logger.info(toJson(responseData));
        }
        return response;
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        if (logger.isInfoEnabled()) {
            Map<String, Object> exceptionData = new LinkedHashMap<>();
            exceptionData.put("invoked", webServiceName);
            exceptionData.put("service", getServiceName(methodTag(configKey)));
            exceptionData.put("duration", elapsedTime / 1000.0 + "s");
            exceptionData.put("exception", ioe.getClass().getSimpleName());
            exceptionData.put("message", ioe.getMessage());
            if (logLevel.ordinal() >= Level.FULL.ordinal() && logger.isDebugEnabled()) {
                exceptionData.put("stackTrace", getStackTrace(ioe));
            }
            logger.warn(toJson(exceptionData));
        }
        return ioe;
    }

    public String decodeOrDefault(byte[] data, Charset charset, String defaultValue) {
        try {
            String bodyString = charset.newDecoder().decode(ByteBuffer.wrap(data)).toString();
            return replaceHelperDecider.replace(bodyString);
        } catch (CharacterCodingException ex) {
            return defaultValue;
        }
    }

    private String getServiceName(String methodTag) {
        return methodTag.substring(1, methodTag.length() - 2);
    }

    private List<String> getStackTrace(Throwable ex) {
        List<String> stackTrace = new ArrayList<>();
        for (StackTraceElement element : ex.getStackTrace()) {
            stackTrace.add(element.toString());
            if (stackTrace.size() > 15) {
                break;
            }
        }
        return stackTrace;
    }

    private String getMaskedQueryString(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return queryString;
        }
        StringBuilder result = new StringBuilder();
        String[] queryParams = queryString.split("&");
        for (String queryParam : queryParams) {
            String[] fieldValueSplit = queryParam.split("=");
            if (fieldValueSplit.length == 2) {
                String maskedValue = replaceHelperDecider.replace(fieldValueSplit[0], fieldValueSplit[1]);
                result.append(fieldValueSplit[0]).append("=").append(maskedValue);
            } else {
                result.append(queryParam);
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private Map<String, Collection<String>> getMaskedHeaders(Map<String, Collection<String>> headers) {
        Map<String, Collection<String>> securedHeaders = new HashMap<>();

        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            Collection<String> headerValues = entry.getValue();
            Collection<String> maskedHeaderValues = new ArrayList<>();
            headerValues.forEach(headerValue -> {
                if (headerValue != null && !headerValue.isEmpty()) {
                    JsonReplaceResultDto jsonReplaceResultDto = replaceHelperDecider.checkJsonAndReplace(headerValue);
                    if (!jsonReplaceResultDto.isJson()) {
                        maskedHeaderValues.add(replaceHelperDecider.replace(headerName, headerValue));
                    } else {
                        maskedHeaderValues.add(jsonReplaceResultDto.getReplacedJson());
                    }
                }
            });
            securedHeaders.put(headerName, maskedHeaderValues);
        }
        return securedHeaders;
    }

    private String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            return "error creating json. " + exception.getMessage();
        }
    }
}
