package com.roze.nexacommerce.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    
    // For validation errors
    private Map<String, String> fieldErrors;
    private List<String> globalErrors;
    
    // For stack trace in development
    private String trace;
    
    public static ErrorResponse of(int status, String error, String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }
}