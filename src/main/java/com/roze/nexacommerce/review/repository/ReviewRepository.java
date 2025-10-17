package com.roze.nexacommerce.review.repository;

import com.roze.nexacommerce.review.dto.ReviewStatisticsProjection;
import com.roze.nexacommerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndActiveTrue(Long productId, Pageable pageable);

    List<Review> findByProductIdAndActiveTrue(Long productId);

    Optional<Review> findByProductIdAndCustomerId(Long productId, Long customerId);

    Page<Review> findByCustomerId(Long customerId, Pageable pageable);

    //    @Query("SELECT new com.roze.nexacommerce.review.dto.ReviewStatistics(" +
//           "COALESCE(AVG(r.rating), 0.0), " +
//           "COUNT(CASE WHEN r.comment IS NOT NULL AND r.comment != '' THEN 1 END), " +
//           "COUNT(r), " +
//           "COUNT(CASE WHEN r.rating = 5 THEN 1 END), " +
//           "COUNT(CASE WHEN r.rating = 4 THEN 1 END), " +
//           "COUNT(CASE WHEN r.rating = 3 THEN 1 END), " +
//           "COUNT(CASE WHEN r.rating = 2 THEN 1 END), " +
//           "COUNT(CASE WHEN r.rating = 1 THEN 1 END)) " +
//           "FROM Review r WHERE r.product.id = :productId AND r.active = true")
//    ReviewStatistics getReviewStatistics(@Param("productId") Long productId);

    @Query("SELECT " +
            "COALESCE(AVG(r.rating), 0.0) as averageRating, " +
            "COUNT(CASE WHEN r.comment IS NOT NULL AND r.comment != '' THEN 1 END) as totalReviews, " +
            "COUNT(r) as totalRatings, " +
            "COUNT(CASE WHEN r.rating = 5 THEN 1 END) as fiveStarCount, " +
            "COUNT(CASE WHEN r.rating = 4 THEN 1 END) as fourStarCount, " +
            "COUNT(CASE WHEN r.rating = 3 THEN 1 END) as threeStarCount, " +
            "COUNT(CASE WHEN r.rating = 2 THEN 1 END) as twoStarCount, " +
            "COUNT(CASE WHEN r.rating = 1 THEN 1 END) as oneStarCount " +
            "FROM Review r WHERE r.product.id = :productId AND r.active = true")
    ReviewStatisticsProjection getReviewStatistics(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.active = true")
    Long countByProductId(@Param("productId") Long productId);

    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);
}