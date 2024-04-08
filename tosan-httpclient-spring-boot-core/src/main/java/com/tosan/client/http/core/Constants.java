package com.tosan.client.http.core;

import com.tosan.tools.mask.starter.business.enumeration.MaskType;
import com.tosan.tools.mask.starter.config.SecureParameter;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public interface Constants {
    SecureParameter AUTHORIZATION_SECURE_PARAM = new SecureParameter("authorization", MaskType.COMPLETE);
    SecureParameter PROXY_AUTHORIZATION_SECURE_PARAM = new SecureParameter("proxy-authorization", MaskType.COMPLETE);
    String X_USER_IP = "X-User-IP";
    String MDC_CLIENT_IP = "clientIP";
    String MDC_REQUEST_ID = "requestId";
    String X_REQUEST_ID = "X-Request-ID";
    String ACCEPT_HEADER = "Accept";
    String CONTENT_TYPE_HEADER = "Content-Type";
}
