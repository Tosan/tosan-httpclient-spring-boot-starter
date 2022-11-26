package com.tosan.client.http.resttemplate.starter.util;

import com.tosan.tools.mask.starter.business.enumeration.MaskType;
import com.tosan.tools.mask.starter.config.SecureParameter;

/**
 * @author Ali Alimohammadi
 * @since 8/3/2022
 */
public interface Constants {
    SecureParameter AUTHORIZATION_SECURE_PARAM = new SecureParameter("authorization", MaskType.COMPLETE);
    SecureParameter PROXY_AUTHORIZATION_SECURE_PARAM = new SecureParameter("proxy-authorization", MaskType.COMPLETE);
}
