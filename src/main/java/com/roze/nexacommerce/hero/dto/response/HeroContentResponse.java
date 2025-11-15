
package com.roze.nexacommerce.hero.dto.response;


import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.enums.TargetAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroContentResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String backgroundImage;
    private String overlayColor;
    private Double overlayOpacity;
    private Boolean active;
    private Integer displayOrder;
    private String button1Text;
    private String button1Url;
    private String button1Color;
    private String button2Text;
    private String button2Url;
    private String button2Color;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private HeroType type;
    private TargetAudience targetAudience;
    private String segmentFilters;
    private Integer impressions;
    private Integer clicks;
    private Double conversionRate;
    private Boolean currentlyActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}