package com.tosan.client.http.sample.server.api.exception;


import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Ali Alimohammadi
 * @since 4/18/2021
 */
@NoArgsConstructor
@ToString(callSuper = true)
public class RequiredParameterException extends ValidationException {
    private static final long serialVersionUID = -2950767067862657987L;

    public RequiredParameterException(String message) {
        super(message);
    }
}
