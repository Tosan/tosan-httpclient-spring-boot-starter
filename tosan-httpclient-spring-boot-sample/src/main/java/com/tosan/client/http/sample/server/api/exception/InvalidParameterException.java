package com.tosan.client.http.sample.server.api.exception;


import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class InvalidParameterException extends ValidationException {
    private static final long serialVersionUID = -2000790838068939848L;

    public InvalidParameterException(String message) {
        super(message);
    }
}
