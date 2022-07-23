package com.tosan.client.http.starter.impl.feign.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 1/22/2021
 */
public abstract class TosanWebServiceRuntimeException extends RuntimeException implements TosanWebServiceBaseException {
    private Map<String, Object> errorParam;
    private String message;

    public TosanWebServiceRuntimeException() {
        super();
    }

    public TosanWebServiceRuntimeException(String message) {
        super(message);
        this.message = message;
    }

    public TosanWebServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public Map<String, Object> getErrorParam() {
        return errorParam;
    }

    public TosanWebServiceRuntimeException addErrorParam(String key, Object value) {
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
