package com.roze.nexacommerce.hero.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.enums.TargetAudience;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hero_contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroContent extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "background_image", nullable = false)
    private String backgroundImage;

    @Column(name = "overlay_color")
    private String overlayColor;

    @Column(name = "overlay_opacity")
    @Builder.Default
    private Double overlayOpacity = 0.8;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "button1_text")
    private String button1Text;

    @Column(name = "button1_url")
    private String button1Url;

    @Column(name = "button1_color")
    private String button1Color;

    @Column(name = "button2_text")
    private String button2Text;

    @Column(name = "button2_url")
    private String button2Url;

    @Column(name = "button2_color")
    private String button2Color;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HeroType type = HeroType.MAIN_BANNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false)
    @Builder.Default
    private TargetAudience targetAudience = TargetAudience.ALL;

    @Column(name = "segment_filters")
    private String segmentFilters;

    @Builder.Default
    private Integer impressions = 0;

    @Builder.Default
    private Integer clicks = 0;

    @Column(name = "conversion_rate")
    @Builder.Default
    private Double conversionRate = 0.0;

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return active &&
                !now.isBefore(startDate) &&
                (endDate == null || !now.isAfter(endDate));
    }

    public void recordImpression() {
        this.impressions++;
        updateConversionRate();
    }

    public void recordClick() {
        this.clicks++;
        updateConversionRate();
    }

    private void updateConversionRate() {
        if (impressions > 0) {
            this.conversionRate = (clicks * 100.0) / impressions;
        }
    }
}