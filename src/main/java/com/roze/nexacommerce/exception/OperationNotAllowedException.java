package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class OperationNotAllowedException extends BaseException {
    public OperationNotAllowedException(String message) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED, "OPERATION_NOT_ALLOWED");
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.METHOD_NOT_ALLOWED, "OPERATION_NOT_ALLOWED");
    }
}