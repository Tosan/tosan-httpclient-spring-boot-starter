package com.tosan.client.http.sample.server.api.exception;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Ali Alimohammadi
 * @since 12/15/2021
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class ValidationException extends CustomServerException {
    private static final long serialVersionUID = -8573482573523085854L;

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public String getErrorType() {
        return "Validation";
    }
}
