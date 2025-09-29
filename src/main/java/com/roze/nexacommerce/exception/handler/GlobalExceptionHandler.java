package com.roze.nexacommerce.exception.handler;

import com.roze.nexacommerce.exception.*;
import com.roze.nexacommerce.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle custom BaseException
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        String path = getRequestPath(request);
        log.error("BaseException: {} - Path: {}", ex.getMessage(), path, ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getErrorCode(),
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // Handle Resource Not Found Exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), path);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.warn("Validation error: {} - Path: {}", ex.getMessage(), path);
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("VALIDATION_FAILED")
                .message("Validation failed for one or more fields")
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle Authentication Exceptions
    @ExceptionHandler({
        BadCredentialsException.class,
        AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        log.warn("Authentication error: {} - Path: {}", ex.getMessage(), path);
        
        String message = "Invalid credentials";
        if (ex instanceof AuthenticationException) {
            message = ex.getMessage();
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "INVALID_CREDENTIALS",
                message,
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle Access Denied Exception
    @ExceptionHandler({AccessDeniedException.class, AuthorizationException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        log.warn("Access denied: {} - Path: {}", ex.getMessage(), path);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Handle Account Status Exceptions
    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponse> handleAccountStatusException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        log.warn("Account status error: {} - Path: {}", ex.getMessage(), path);
        
        String message = "Account is not accessible";
        String errorCode = "ACCOUNT_DISABLED";
        
        if (ex instanceof LockedException) {
            message = "Account is locked";
            errorCode = "ACCOUNT_LOCKED";
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                errorCode,
                message,
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle Duplicate Resource Exception
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.warn("Duplicate resource: {} - Path: {}", ex.getMessage(), path);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Handle Business Logic Exceptions
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        String path = getRequestPath(request);
        log.warn("Business error: {} - Path: {}", ex.getMessage(), path);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "BUSINESS_ERROR",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle File Operation Exceptions
    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ErrorResponse> handleFileOperationException(
            FileOperationException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.error("File operation error: {} - Path: {}", ex.getMessage(), path, ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "FILE_OPERATION_ERROR",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle External Service Exceptions
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.error("External service error: {} - Path: {}", ex.getMessage(), path, ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "EXTERNAL_SERVICE_ERROR",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Handle Rate Limit Exceptions
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(
            RateLimitException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        log.warn("Rate limit exceeded: {} - Path: {}", ex.getMessage(), path);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    // Handle All Other Exceptions (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), path, ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                path
        );
        
        // Include stack trace only in development
        if (isDevelopment()) {
            errorResponse.setTrace(getStackTrace(ex));
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Utility methods
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
            return servletRequest.getRequestURI();
        }
        return "Unknown";
    }

    private String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private boolean isDevelopment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null && profile.contains("dev");
    }
}