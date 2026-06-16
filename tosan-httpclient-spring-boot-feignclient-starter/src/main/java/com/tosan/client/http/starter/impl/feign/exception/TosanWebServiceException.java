package com.tosan.client.http.starter.impl.feign.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public abstract class TosanWebServiceException extends Exception implements TosanWebServiceBaseException {
    private Map<String, Object> errorParam;
    private String message;

    public TosanWebServiceException() {
        super();
    }

    public TosanWebServiceException(String message) {
        super(message);
        this.message = message;
    }

    public TosanWebServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public Map<String, Object> getErrorParam() {
        return errorParam;
    }

    public void setErrorParam(Map<String, Object> errorParam) {
        this.errorParam = errorParam;
    }

    public TosanWebServiceException addErrorParam(String key, Object value) {
        if (errorParam == null) {
            errorParam = new HashMap<>();
        }
        errorParam.put(key, value);
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
