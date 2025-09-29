package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }
}