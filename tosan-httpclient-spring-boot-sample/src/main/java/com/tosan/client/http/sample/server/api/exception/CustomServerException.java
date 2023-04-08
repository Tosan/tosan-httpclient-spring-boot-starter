package com.tosan.client.http.sample.server.api.exception;

import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceException;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@NoArgsConstructor
public abstract class CustomServerException extends TosanWebServiceException {
    @Serial
    private static final long serialVersionUID = 6543428451282914405L;

    public CustomServerException(String message) {
        super(message);
    }

    public CustomServerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }
}
