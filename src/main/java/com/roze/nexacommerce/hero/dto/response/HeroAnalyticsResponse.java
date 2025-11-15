package com.roze.nexacommerce.hero.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroAnalyticsResponse {
    private Long heroContentId;
    private String title;
    private Integer totalImpressions;
    private Integer totalClicks;
    private Double conversionRate;
    private String performanceStatus;
}