package com.roze.nexacommerce.review.mapper;

import com.roze.nexacommerce.review.dto.response.ReviewResponse;
import com.roze.nexacommerce.review.entity.Review;
import com.roze.nexacommerce.review.repository.ReviewHelpfulVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;

    public ReviewResponse toResponse(Review review, Long currentCustomerId) {
        ReviewResponse response = ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getUser().getName())
                .customerProfileImage(review.getCustomer().getProfileImage())
                .rating(review.getRating())
                .comment(review.getComment())
                .title(review.getTitle())
                .verifiedPurchase(review.getVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .notHelpfulCount(review.getNotHelpfulCount())
                .active(review.getActive())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();

        // Add user's vote status if customer ID is provided
        if (currentCustomerId != null) {
            helpfulVoteRepository.findByReviewIdAndCustomerId(review.getId(), currentCustomerId)
                    .ifPresent(vote -> {
                        response.setUserVotedHelpful(vote.getHelpful());
                        response.setUserVotedNotHelpful(!vote.getHelpful());
                    });
        }

        return response;
    }
}