package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseException {
    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
