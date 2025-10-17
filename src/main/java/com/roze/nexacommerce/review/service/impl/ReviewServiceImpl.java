package com.roze.nexacommerce.review.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.repository.OrderRepository;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.review.dto.ReviewStatisticsProjection;
import com.roze.nexacommerce.review.dto.request.CreateReviewRequest;
import com.roze.nexacommerce.review.dto.request.UpdateReviewRequest;
import com.roze.nexacommerce.review.dto.response.RatingDistribution;
import com.roze.nexacommerce.review.dto.response.ReviewResponse;
import com.roze.nexacommerce.review.dto.response.ReviewSummaryResponse;
import com.roze.nexacommerce.review.entity.Review;
import com.roze.nexacommerce.review.entity.ReviewHelpfulVote;
import com.roze.nexacommerce.review.mapper.ReviewMapper;
import com.roze.nexacommerce.review.repository.ReviewHelpfulVoteRepository;
import com.roze.nexacommerce.review.repository.ReviewRepository;
import com.roze.nexacommerce.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;
    private final ProductRepository productRepository;
    private final CustomerProfileRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long customerId) {
        // Check if customer already reviewed this product
        if (reviewRepository.existsByProductIdAndCustomerId(request.getProductId(), customerId)) {
            throw new DuplicateResourceException("Review", "product_id and customer_id",
                    request.getProductId() + "-" + customerId);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // Check if customer has purchased the product (for verified purchase badge)
        boolean verifiedPurchase = hasCustomerPurchasedProduct(customerId, request.getProductId());

        Review review = Review.builder()
                .product(product)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment())
                .title(request.getTitle())
                .verifiedPurchase(verifiedPurchase)
                .active(true)
                .build();

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview, customerId);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getActive() != null) {
            review.setActive(request.getActive());
        }

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(updatedReview, review.getCustomer().getId());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        reviewRepository.delete(review);
    }

    @Override
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return reviewMapper.toResponse(review, review.getCustomer().getId());
    }

    @Override
    public PaginatedResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByProductIdAndActiveTrue(productId, pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(review -> reviewMapper.toResponse(review, null)) // Don't include user vote status for public
                .toList();

        return PaginatedResponse.<ReviewResponse>builder()
                .items(reviewResponses)
                .totalItems(reviewPage.getTotalElements())
                .currentPage(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    @Override
    public PaginatedResponse<ReviewResponse> getCustomerReviews(Long customerId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByCustomerId(customerId, pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(review -> reviewMapper.toResponse(review, customerId))
                .toList();

        return PaginatedResponse.<ReviewResponse>builder()
                .items(reviewResponses)
                .totalItems(reviewPage.getTotalElements())
                .currentPage(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    //    @Override
//    public ReviewSummaryResponse getProductReviewSummary(Long productId) {
//        ReviewStatistics statistics = reviewRepository.getReviewStatistics(productId);
//
//        RatingDistribution distribution =
//           RatingDistribution.builder()
//                .fiveStar(statistics.getFiveStarCount())
//                .fourStar(statistics.getFourStarCount())
//                .threeStar(statistics.getThreeStarCount())
//                .twoStar(statistics.getTwoStarCount())
//                .oneStar(statistics.getOneStarCount())
//                .build();
//
//        return ReviewSummaryResponse.builder()
//                .averageRating(statistics.getAverageRating())
//                .totalReviews(statistics.getTotalReviews())
//                .totalRatings(statistics.getTotalRatings())
//                .distribution(distribution)
//                .build();
//    }
    @Override
    public ReviewSummaryResponse getProductReviewSummary(Long productId) {
        ReviewStatisticsProjection projection = reviewRepository.getReviewStatistics(productId);

        // Handle null projection (no reviews)
        if (projection == null || projection.getTotalRatings() == null || projection.getTotalRatings() == 0) {
            return createEmptyReviewSummary();
        }

        RatingDistribution distribution = RatingDistribution.builder()
                .fiveStar(projection.getFiveStarCount() != null ? projection.getFiveStarCount().intValue() : 0)
                .fourStar(projection.getFourStarCount() != null ? projection.getFourStarCount().intValue() : 0)
                .threeStar(projection.getThreeStarCount() != null ? projection.getThreeStarCount().intValue() : 0)
                .twoStar(projection.getTwoStarCount() != null ? projection.getTwoStarCount().intValue() : 0)
                .oneStar(projection.getOneStarCount() != null ? projection.getOneStarCount().intValue() : 0)
                .build();

        return ReviewSummaryResponse.builder()
                .averageRating(projection.getAverageRating() != null ?
                        Math.round(projection.getAverageRating() * 100.0) / 100.0 : 0.0)
                .totalReviews(projection.getTotalReviews() != null ? projection.getTotalReviews().intValue() : 0)
                .totalRatings(projection.getTotalRatings() != null ? projection.getTotalRatings().intValue() : 0)
                .distribution(distribution)
                .build();
    }

    private ReviewSummaryResponse createEmptyReviewSummary() {
        return ReviewSummaryResponse.builder()
                .averageRating(0.0)
                .totalReviews(0)
                .totalRatings(0)
                .distribution(RatingDistribution.builder()
                        .fiveStar(0)
                        .fourStar(0)
                        .threeStar(0)
                        .twoStar(0)
                        .oneStar(0)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void markReviewHelpful(Long reviewId, Long customerId) {
        handleHelpfulVote(reviewId, customerId, true);
    }

    @Override
    @Transactional
    public void markReviewNotHelpful(Long reviewId, Long customerId) {
        handleHelpfulVote(reviewId, customerId, false);
    }

    private void handleHelpfulVote(Long reviewId, Long customerId, Boolean helpful) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // Check if customer already voted
        helpfulVoteRepository.findByReviewIdAndCustomerId(reviewId, customerId)
                .ifPresentOrElse(
                        existingVote -> {
                            // Update existing vote
                            if (!existingVote.getHelpful().equals(helpful)) {
                                existingVote.setHelpful(helpful);
                                helpfulVoteRepository.save(existingVote);
                                updateReviewHelpfulCounts(review);
                            }
                        },
                        () -> {
                            // Create new vote
                            ReviewHelpfulVote vote = ReviewHelpfulVote.builder()
                                    .review(review)
                                    .customer(customer)
                                    .helpful(helpful)
                                    .build();
                            helpfulVoteRepository.save(vote);
                            updateReviewHelpfulCounts(review);
                        }
                );
    }

    private void updateReviewHelpfulCounts(Review review) {
        Long helpfulCount = helpfulVoteRepository.countByReviewIdAndHelpfulTrue(review.getId());
        Long notHelpfulCount = helpfulVoteRepository.countByReviewIdAndHelpfulFalse(review.getId());

        review.setHelpfulCount(helpfulCount.intValue());
        review.setNotHelpfulCount(notHelpfulCount.intValue());
        reviewRepository.save(review);
    }

    @Override
    public boolean hasCustomerPurchasedProduct(Long customerId, Long productId) {
        // This would typically check order history
        List<Order> customerOrders = orderRepository.findByCustomerIdAndStatusIn(
                customerId, List.of("DELIVERED"));

        return customerOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }

    @Override
    public boolean hasCustomerReviewedProduct(Long customerId, Long productId) {
        return reviewRepository.existsByProductIdAndCustomerId(productId, customerId);
    }
}