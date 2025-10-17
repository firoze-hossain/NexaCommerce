package com.roze.nexacommerce.review.repository;

import com.roze.nexacommerce.review.entity.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, Long> {
    
    Optional<ReviewHelpfulVote> findByReviewIdAndCustomerId(Long reviewId, Long customerId);
    
    boolean existsByReviewIdAndCustomerId(Long reviewId, Long customerId);
    
    Long countByReviewIdAndHelpfulTrue(Long reviewId);
    
    Long countByReviewIdAndHelpfulFalse(Long reviewId);
}