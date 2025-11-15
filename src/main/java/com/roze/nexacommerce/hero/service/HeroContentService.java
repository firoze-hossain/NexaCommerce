
package com.roze.nexacommerce.hero.service;

import com.roze.nexacommerce.hero.dto.request.HeroContentRequest;
import com.roze.nexacommerce.hero.dto.response.HeroAnalyticsResponse;
import com.roze.nexacommerce.hero.dto.response.HeroContentResponse;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.user.entity.User;

import java.util.List;

public interface HeroContentService {

    // Content Management
    HeroContentResponse createHeroContent(HeroContentRequest request);

    HeroContentResponse getHeroContentById(Long id);

    List<HeroContentResponse> getAllHeroContent();

    HeroContentResponse updateHeroContent(Long id, HeroContentRequest request);

    void deleteHeroContent(Long id);

    HeroContentResponse toggleHeroContentStatus(Long id);

    // Public API
    List<HeroContentResponse> getActiveHeroContent(User currentUser, String userSegment);

    HeroContentResponse getMainHeroContent(User currentUser, String userSegment);

    List<HeroContentResponse> getHeroContentByType(HeroType type, User currentUser, String userSegment);

    // Analytics
    void recordImpression(Long heroContentId);

    void recordClick(Long heroContentId);

    List<HeroAnalyticsResponse> getHeroAnalytics();

    HeroAnalyticsResponse getHeroContentAnalytics(Long id);

    // Maintenance
    void deactivateExpiredContent();

    List<HeroContentResponse> getTopPerformingContent();
}