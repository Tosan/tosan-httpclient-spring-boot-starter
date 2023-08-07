package com.tosan.client.http.sample.server.rest.exceptionhandler;

import com.tosan.client.http.sample.server.api.exception.CustomServerException;
import com.tosan.client.http.starter.impl.feign.ErrorObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Alimohammadi
 * @since 12/14/2020
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({CustomServerException.class})
    @ResponseBody
    public ResponseEntity<ErrorObject> exception(CustomServerException customServerException) {
        ErrorObject errorObject = convertCustomServerExceptionToErrorObject(customServerException);
        return ResponseEntity.badRequest().body(errorObject);
    }

    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public ResponseEntity<ErrorObject> exception(Throwable throwable) {
        ErrorObject errorObject = convertThrowableToErrorObject(throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorObject);
    }

    private ErrorObject convertCustomServerExceptionToErrorObject(CustomServerException customServerException) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setErrorType(customServerException.getErrorType());
        errorObject.setErrorCode(customServerException.getErrorCode());
        errorObject.setMessage(customServerException.getMessage());
        errorObject.setErrorParam(customServerException.getErrorParam());
        return errorObject;
    }

    private ErrorObject convertThrowableToErrorObject(Throwable throwable) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setErrorType("throwable");
        errorObject.setErrorCode(throwable.getClass().getSimpleName());
        errorObject.setMessage(throwable.getMessage());
        Map<String, Object> errorParam = new HashMap<>();
        errorParam.put("localizedMessage", throwable.getLocalizedMessage());
        errorObject.setErrorParam(errorParam);
        return errorObject;
    }
}
