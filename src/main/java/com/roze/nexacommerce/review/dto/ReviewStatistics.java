package com.roze.nexacommerce.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatistics {
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalRatings;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;

    public Integer getTotalCount() {
        return totalReviews + totalRatings;
    }

    public Double getAverageRating() {
        if (totalRatings == 0) return 0.0;
        return Math.round(averageRating * 100.0) / 100.0; // Round to 2 decimal places
    }
}