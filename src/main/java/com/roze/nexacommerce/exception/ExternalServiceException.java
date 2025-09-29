package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BaseException {
    public ExternalServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR");
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR");
    }
}