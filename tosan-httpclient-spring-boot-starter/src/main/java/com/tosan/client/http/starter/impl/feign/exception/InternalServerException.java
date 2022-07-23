package com.tosan.client.http.starter.impl.feign.exception;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public class InternalServerException extends RuntimeException {
    private String errorType;
    private String errorCode;
    private String message;
    private Map<String, Object> errorParam;
    private String jsonResponse;

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getErrorParam() {
        return errorParam;
    }

    public void setErrorParam(Map<String, Object> errorParam) {
        this.errorParam = errorParam;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("errorType", errorType)
                .append("errorCode", errorCode)
                .append("message", message)
                .append("errorParam", errorParam)
                .append("jsonResponse", jsonResponse)
                .toString();
    }
}
