package com.roze.nexacommerce.review.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.review.dto.request.CreateReviewRequest;
import com.roze.nexacommerce.review.dto.request.UpdateReviewRequest;
import com.roze.nexacommerce.review.dto.response.ReviewResponse;
import com.roze.nexacommerce.review.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request, Long customerId);

    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request);

    void deleteReview(Long reviewId);

    ReviewResponse getReviewById(Long reviewId);

    PaginatedResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    PaginatedResponse<ReviewResponse> getCustomerReviews(Long customerId, Pageable pageable);

    ReviewSummaryResponse getProductReviewSummary(Long productId);

    void markReviewHelpful(Long reviewId, Long customerId);

    void markReviewNotHelpful(Long reviewId, Long customerId);

    boolean hasCustomerPurchasedProduct(Long customerId, Long productId);

    boolean hasCustomerReviewedProduct(Long customerId, Long productId);
}