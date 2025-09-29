package com.roze.nexacommerce.exception;

import org.springframework.http.HttpStatus;

public class RateLimitException extends BaseException {
    public RateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}