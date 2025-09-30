package com.roze.nexacommerce.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public class BaseController {
    /**
     * 200 OK response with default message
     */
    protected <T> ResponseEntity<BaseResponse<T>> ok(T data) {
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    /**
     * 200 OK response with custom message
     */
    protected <T> ResponseEntity<BaseResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(BaseResponse.success(data, message));
    }

    /**
     * 201 Created response with default message
     */
    protected <T> ResponseEntity<BaseResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(data));
    }

    /**
     * 201 Created response with custom message
     */
    protected <T> ResponseEntity<BaseResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(data, message));
    }

    /**
     * 202 Accepted response
     */
    protected <T> ResponseEntity<BaseResponse<T>> accepted(T data, String message) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(BaseResponse.accepted(data, message));
    }

    /**
     * 204 No Content response with default message
     */
    protected ResponseEntity<BaseResponse<Void>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.noContent());
    }

    /**
     * 204 No Content response with custom message
     */
    protected ResponseEntity<BaseResponse<Void>> noContent(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.noContent(message));
    }

    /**
     * 200 OK with PaginatedResponse from Spring Page object
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(Page<T> page) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.fromPage(page);
        return ResponseEntity.ok(BaseResponse.success(paginatedResponse, "Data retrieved successfully"));
    }

    /**
     * 200 OK with PaginatedResponse from Spring Page object with custom message
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(Page<T> page, String message) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.fromPage(page);
        return ResponseEntity.ok(BaseResponse.success(paginatedResponse, message));
    }

    /**
     * 200 OK with existing PaginatedResponse and custom message
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(PaginatedResponse<T> paginatedResponse, String message) {
        return ResponseEntity.ok(BaseResponse.success(paginatedResponse, message));
    }

    /**
     * 200 OK with custom PaginatedResponse
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(List<T> items, long totalItems, int currentPage, int pageSize, int totalPages) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.of(items, totalItems, currentPage, pageSize, totalPages);
        return ResponseEntity.ok(BaseResponse.success(paginatedResponse, "Data retrieved successfully"));
    }

    /**
     * 200 OK with custom PaginatedResponse and custom message
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(List<T> items, long totalItems, int currentPage, int pageSize, int totalPages, String message) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.of(items, totalItems, currentPage, pageSize, totalPages);
        return ResponseEntity.ok(BaseResponse.success(paginatedResponse, message));
    }

    /**
     * Generic error response
     */
    protected <T> ResponseEntity<BaseResponse<T>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponse.error(message, status.value()));
    }

    /**
     * Error response with data
     */
    protected <T> ResponseEntity<BaseResponse<T>> error(String message, HttpStatus status, T data) {
        BaseResponse<T> response = BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(status.value())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 400 Bad Request
     */
    protected <T> ResponseEntity<BaseResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * 401 Unauthorized
     */
    protected <T> ResponseEntity<BaseResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * 403 Forbidden
     */
    protected <T> ResponseEntity<BaseResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * 404 Not Found
     */
    protected <T> ResponseEntity<BaseResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * 409 Conflict
     */
    protected <T> ResponseEntity<BaseResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * 500 Internal Server Error
     */
    protected <T> ResponseEntity<BaseResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.badRequest(message));
    }

    /**
     * Success response for boolean operations
     */
    protected ResponseEntity<BaseResponse<Boolean>> success(boolean result, String successMessage, String errorMessage) {
        if (result) {
            return ok(true, successMessage);
        } else {
            return badRequest(errorMessage);
        }
    }

    /**
     * Success response for operations that return affected count
     */
    protected ResponseEntity<BaseResponse<Integer>> operationResult(int affectedCount, String successMessage, String errorMessage) {
        if (affectedCount > 0) {
            return ok(affectedCount, successMessage);
        } else {
            return badRequest(errorMessage);
        }
    }

    /**
     * Success response for delete operations
     */
    protected ResponseEntity<BaseResponse<Boolean>> deleteResult(boolean deleted, String successMessage) {
        if (deleted) {
            return ok(true, successMessage);
        } else {
            return notFound("Resource not found");
        }
    }
}
