package com.tosan.client.http.starter.util;

import com.tosan.tools.mask.starter.config.SecureParametersConfig;

/**
 * @author Ali Alimohammadi
 * @since 11/15/2023
 */
public class HttpReplaceHelperDecider {

    private final KeyValueReplaceHelper keyValueReplaceHelper;
    private final SecureParametersConfig secureParametersConfig;

    public HttpReplaceHelperDecider(KeyValueReplaceHelper keyValueReplaceHelper,
                                    SecureParametersConfig secureParametersConfig) {
        this.keyValueReplaceHelper = keyValueReplaceHelper;
        this.secureParametersConfig = secureParametersConfig;
    }

    public String keyValueHeaderReplace(String keyValueString) {
        try {
            return this.keyValueReplaceHelper.replace(keyValueString, this.secureParametersConfig.getSecuredParametersMap());
        } catch (Exception e) {
            return keyValueString;
        }
    }
}
