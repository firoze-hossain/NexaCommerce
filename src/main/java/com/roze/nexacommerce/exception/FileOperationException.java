package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class FileOperationException extends BaseException {
    public FileOperationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_OPERATION_ERROR");
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_OPERATION_ERROR");
    }
}