package com.tosan.client.http.starter.impl.feign.exception;

import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 5/10/2021
 */
public interface TosanWebServiceBaseException {

    String getErrorType();

    String getErrorCode();

    String getMessage();

    Map<String, Object> getErrorParam();

    TosanWebServiceBaseException addErrorParam(String key, Object value);
}
