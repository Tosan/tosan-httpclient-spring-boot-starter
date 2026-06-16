package com.tosan.client.http.restclient.starter.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.RawValue;
import com.tosan.tools.mask.starter.dto.JsonReplaceResultDto;
import com.tosan.tools.mask.starter.replace.JsonReplaceHelperDecider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public class HttpLoggingInterceptorUtil {
    private static final ObjectMapper mapper = new JsonMapper();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final JsonReplaceHelperDecider replaceHelperDecider;

    public HttpLoggingInterceptorUtil(JsonReplaceHelperDecider replaceHelperDecider) {
        this.replaceHelperDecider = replaceHelperDecider;
    }


    public String getRequestDetailContent(HttpRequest request, byte[] body, String webServiceName) {
        final Map<String, Object> requestData = new LinkedHashMap<>();
        requestData.put("invoke", webServiceName);
        String urlString = request.getURI().toString();
        boolean hasQueryString = urlString.contains("?");
        if (hasQueryString) {
            String[] splitUrl = urlString.split("[?]");
            String maskedQueryString = splitUrl.length > 1 ? getUrlEncodedString(splitUrl[1]) : "";
            requestData.put("service", request.getMethod() + " " + splitUrl[0] + "?" + maskedQueryString);
        } else {
            requestData.put("service", request.getMethod() + " " + urlString);
        }
        if (!request.getHeaders().isEmpty()) {
            requestData.put("headers", getMaskedHeaders(request.getHeaders()));
        }
        if (body != null) {
            String maskedBody;
            if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType()
                    .equalsTypeAndSubtype(MediaType.APPLICATION_FORM_URLENCODED)) {
                maskedBody = getUrlEncodedString(new String(body, StandardCharsets.UTF_8));
            } else {
                maskedBody = replaceHelperDecider.replace(new String(body, StandardCharsets.UTF_8));
            }
            requestData.put("body", new RawValue(maskedBody));
        }
        return toJson(requestData);
    }

    public String getResponseDetailContent(ClientHttpResponse response, String webServiceName, long elapsedTime) throws IOException {
        final Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("invoked", webServiceName);
        responseData.put("duration", elapsedTime / 1000.0 + "s");
        responseData.put("status", response.getStatusCode());
        if (!response.getHeaders().isEmpty()) {
            responseData.put("headers", getMaskedHeaders(response.getHeaders()));
        }
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        if (StringUtils.isNotEmpty(responseBody)) {
            String securedBody = replaceHelperDecider.replace(responseBody);
            responseData.put("body", new RawValue(securedBody));
        }
        return toJson(responseData);
    }

    public String getExceptionDetailContent(Exception exception, String webServiceName, long elapsedTime) {
        final Map<String, Object> exceptionData = new LinkedHashMap<>();
        exceptionData.put("invoked", webServiceName);
        exceptionData.put("duration", elapsedTime / 1000.0 + "s");
        exceptionData.put("exception", exception.getClass().getSimpleName());
        exceptionData.put("message", exception.getMessage());
        return toJson(exceptionData);
    }

    private HttpHeaders getMaskedHeaders(HttpHeaders headers) {
        HttpHeaders securedHeaders = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : headers.headerSet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            List<String> maskedHeaderValues = new ArrayList<>();
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

    private String getUrlEncodedString(String queryString) {
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

    private String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            return "error creating json. " + exception.getMessage();
        }
    }
}
