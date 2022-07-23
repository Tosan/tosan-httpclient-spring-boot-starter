package com.tosan.client.http.starter.impl.feign;

import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 1/20/2021
 */
public class ErrorObject {
    private String errorType;
    private String errorCode;
    private String message;
    private Map<String, Object> errorParam;

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
}
