package com.roze.nexacommerce.review.dto;

public interface ReviewStatisticsProjection {
    Double getAverageRating();
    Long getTotalReviews();
    Long getTotalRatings();
    Long getFiveStarCount();
    Long getFourStarCount();
    Long getThreeStarCount();
    Long getTwoStarCount();
    Long getOneStarCount();
}