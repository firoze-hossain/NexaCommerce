package com.roze.nexacommerce.review.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.review.dto.request.CreateReviewRequest;
import com.roze.nexacommerce.review.dto.request.UpdateReviewRequest;
import com.roze.nexacommerce.review.dto.response.ReviewResponse;
import com.roze.nexacommerce.review.dto.response.ReviewSummaryResponse;
import com.roze.nexacommerce.review.service.ReviewService;
import com.roze.nexacommerce.security.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController extends BaseController {

    private final ReviewService reviewService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<BaseResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found or user is not a customer");
        }
        ReviewResponse response = reviewService.createReview(request, customerId);
        return created(response, "Review created successfully");
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('CUSTOMER') and @securityService.isReviewOwner(#reviewId)")
    public ResponseEntity<BaseResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found or user is not a customer");
        }
        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ok(response, "Review updated successfully");
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('CUSTOMER') and @securityService.isReviewOwner(#reviewId)")
    public ResponseEntity<BaseResponse<Void>> deleteReview(
            @PathVariable Long reviewId) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found or user is not a customer");
        }
        reviewService.deleteReview(reviewId);
        return noContent("Review deleted successfully");
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<BaseResponse<PaginatedResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<ReviewResponse> reviews = reviewService.getProductReviews(productId, pageable);
        return paginated(reviews, "Reviews retrieved successfully");
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<BaseResponse<ReviewSummaryResponse>> getProductReviewSummary(
            @PathVariable Long productId) {
        ReviewSummaryResponse summary = reviewService.getProductReviewSummary(productId);
        return ok(summary, "Review summary retrieved successfully");
    }

    @PostMapping("/{reviewId}/helpful")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<BaseResponse<Void>> markReviewHelpful(
            @PathVariable Long reviewId) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found or user is not a customer");
        }
        reviewService.markReviewHelpful(reviewId, customerId);
        return ok(null, "Review marked as helpful");
    }

    @PostMapping("/{reviewId}/not-helpful")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<BaseResponse<Void>> markReviewNotHelpful(
            @PathVariable Long reviewId) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found or user is not a customer");
        }
        reviewService.markReviewNotHelpful(reviewId, customerId);
        return ok(null, "Review marked as not helpful");
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('CUSTOMER') and @securityService.isCurrentUser(#customerId)")
    public ResponseEntity<BaseResponse<PaginatedResponse<ReviewResponse>>> getCustomerReviews(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<ReviewResponse> reviews = reviewService.getCustomerReviews(customerId, pageable);
        return paginated(reviews, "Customer reviews retrieved successfully");
    }
}