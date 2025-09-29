package com.roze.nexacommerce.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Instant timestamp;
    private Integer statusCode;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(Instant.now())
                .statusCode(200)
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(200)
                .build();
    }

    public static <T> BaseResponse<T> created(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .timestamp(Instant.now())
                .statusCode(201)
                .build();
    }

    public static <T> BaseResponse<T> created(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(201)
                .build();
    }

    public static BaseResponse<Void> noContent() {
        return BaseResponse.<Void>builder()
                .success(true)
                .message("No content")
                .data(null)
                .timestamp((Instant.now()))
                .statusCode(204)
                .build();
    }

    public static BaseResponse<Void> noContent(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .statusCode(204)
                .build();
    }

    public static <T> BaseResponse<T> accepted(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(202)
                .build();
    }

    public static <T> BaseResponse<T> error(String message, Integer statusCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .statusCode(statusCode)
                .build();
    }

    public static <T> BaseResponse<T> badRequest(String message) {
        return error(message, 400);
    }

    public static <T> BaseResponse<T> unauthorized(String message) {
        return error(message, 401);
    }

    public static <T> BaseResponse<T> forbidden(String message) {
        return error(message, 403);
    }

    public static <T> BaseResponse<T> notFound(String message) {
        return error(message, 404);
    }

    public static <T> BaseResponse<T> conflict(String message) {
        return error(message, 409);
    }

    public static <T> BaseResponse<T> internalError(String message) {
        return error(message, 500);
    }
}
