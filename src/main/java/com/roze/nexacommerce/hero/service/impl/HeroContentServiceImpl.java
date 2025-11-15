
package com.roze.nexacommerce.hero.service.impl;

import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.hero.dto.request.HeroContentRequest;
import com.roze.nexacommerce.hero.dto.response.HeroAnalyticsResponse;
import com.roze.nexacommerce.hero.dto.response.HeroContentResponse;
import com.roze.nexacommerce.hero.entity.HeroContent;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.enums.TargetAudience;
import com.roze.nexacommerce.hero.mapper.HeroContentMapper;
import com.roze.nexacommerce.hero.repository.HeroContentRepository;
import com.roze.nexacommerce.hero.service.HeroContentService;
import com.roze.nexacommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeroContentServiceImpl implements HeroContentService {

    private final HeroContentRepository heroContentRepository;
    private final HeroContentMapper heroContentMapper;

    @Override
    @Transactional
    public HeroContentResponse createHeroContent(HeroContentRequest request) {
        log.info("Creating new hero content with title: {}", request.getTitle());

        HeroContent heroContent = heroContentMapper.toEntity(request);
        HeroContent savedContent = heroContentRepository.save(heroContent);

        log.info("Successfully created hero content with ID: {}", savedContent.getId());
        return heroContentMapper.toResponse(savedContent);
    }

    @Override
    @Transactional(readOnly = true)
    public HeroContentResponse getHeroContentById(Long id) {
        log.debug("Fetching hero content by ID: {}", id);

        HeroContent heroContent = heroContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content", "id", id));

        return heroContentMapper.toResponse(heroContent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroContentResponse> getAllHeroContent() {
        log.debug("Fetching all hero content");

        return heroContentRepository.findAll().stream()
                .map(heroContentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HeroContentResponse updateHeroContent(Long id, HeroContentRequest request) {
        log.info("Updating hero content with ID: {}", id);

        HeroContent heroContent = heroContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content", "id", id));

        heroContentMapper.updateEntityFromRequest(request, heroContent);
        HeroContent updatedContent = heroContentRepository.save(heroContent);

        log.info("Successfully updated hero content with ID: {}", id);
        return heroContentMapper.toResponse(updatedContent);
    }

    @Override
    @Transactional
    public void deleteHeroContent(Long id) {
        log.info("Deleting hero content with ID: {}", id);

        HeroContent heroContent = heroContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content", "id", id));

        heroContentRepository.delete(heroContent);
        log.info("Successfully deleted hero content with ID: {}", id);
    }

    @Override
    @Transactional
    public HeroContentResponse toggleHeroContentStatus(Long id) {
        log.info("Toggling status for hero content with ID: {}", id);

        HeroContent heroContent = heroContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content", "id", id));

        heroContent.setActive(!heroContent.getActive());
        HeroContent updatedContent = heroContentRepository.save(heroContent);

        log.info("Hero content ID: {} status set to: {}", id, updatedContent.getActive());
        return heroContentMapper.toResponse(updatedContent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroContentResponse> getActiveHeroContent(User currentUser, String userSegment) {
        log.debug("Fetching active hero content for user segment: {}", userSegment);

        List<HeroContent> activeContent = heroContentRepository.findActiveContent(LocalDateTime.now());

        return activeContent.stream()
                .filter(content -> isContentRelevant(content, currentUser, userSegment))
                .map(heroContentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HeroContentResponse getMainHeroContent(User currentUser, String userSegment) {
        log.debug("Fetching main hero content for user segment: {}", userSegment);

        List<HeroContent> activeContent = heroContentRepository.findActiveContent(LocalDateTime.now());

        return activeContent.stream()
                .filter(content -> isContentRelevant(content, currentUser, userSegment))
                .filter(content -> content.getType() == HeroType.MAIN_BANNER)
                .findFirst()
                .map(heroContentMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroContentResponse> getHeroContentByType(HeroType type, User currentUser, String userSegment) {
        log.debug("Fetching hero content by type: {} for user segment: {}", type, userSegment);

        List<HeroContent> typeContent = heroContentRepository.findActiveByType(type, LocalDateTime.now());

        return typeContent.stream()
                .filter(content -> isContentRelevant(content, currentUser, userSegment))
                .map(heroContentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordImpression(Long heroContentId) {
        log.debug("Recording impression for hero content ID: {}", heroContentId);

        heroContentRepository.findById(heroContentId).ifPresent(heroContent -> {
            heroContent.recordImpression();
            heroContentRepository.save(heroContent);
        });
    }

    @Override
    @Transactional
    public void recordClick(Long heroContentId) {
        log.debug("Recording click for hero content ID: {}", heroContentId);

        heroContentRepository.findById(heroContentId).ifPresent(heroContent -> {
            heroContent.recordClick();
            heroContentRepository.save(heroContent);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroAnalyticsResponse> getHeroAnalytics() {
        log.debug("Fetching hero content analytics");

        return heroContentRepository.findAll().stream()
                .map(this::toAnalyticsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HeroAnalyticsResponse getHeroContentAnalytics(Long id) {
        log.debug("Fetching analytics for hero content ID: {}", id);

        HeroContent heroContent = heroContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero content", "id", id));

        return toAnalyticsResponse(heroContent);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void deactivateExpiredContent() {
        log.info("Starting deactivation of expired hero content");

        int deactivatedCount = heroContentRepository.deactivateExpiredContent(LocalDateTime.now());
        log.info("Deactivated {} expired hero content items", deactivatedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeroContentResponse> getTopPerformingContent() {
        log.debug("Fetching top performing hero content");

        return heroContentRepository.findTopPerformingContent().stream()
                .map(heroContentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isContentRelevant(HeroContent content, User currentUser, String userSegment) {
        TargetAudience targetAudience = content.getTargetAudience();

        if (targetAudience == TargetAudience.ALL) {
            return true;
        }

        if (currentUser == null) {
            return targetAudience == TargetAudience.GUEST;
        }

        switch (targetAudience) {
            case CUSTOMER:
                return currentUser.isCustomer();
            case NEW_CUSTOMER:
                return currentUser.isCustomer() && isNewCustomer(currentUser);
            case RETURNING_CUSTOMER:
                return currentUser.isCustomer() && !isNewCustomer(currentUser);
            case VIP_CUSTOMER:
                return currentUser.isCustomer() && isVipCustomer(currentUser, userSegment);
            default:
                return true;
        }
    }

    private boolean isNewCustomer(User user) {
        // Implement logic to determine if customer is new
        // This could be based on registration date or order count
        return true; // Simplified for example
    }

    private boolean isVipCustomer(User user, String userSegment) {
        // Implement VIP customer logic based on spending, loyalty points, etc.
        return "VIP".equals(userSegment);
    }

    private HeroAnalyticsResponse toAnalyticsResponse(HeroContent heroContent) {
        String performanceStatus = calculatePerformanceStatus(heroContent);

        return HeroAnalyticsResponse.builder()
                .heroContentId(heroContent.getId())
                .title(heroContent.getTitle())
                .totalImpressions(heroContent.getImpressions())
                .totalClicks(heroContent.getClicks())
                .conversionRate(heroContent.getConversionRate())
                .performanceStatus(performanceStatus)
                .build();
    }

    private String calculatePerformanceStatus(HeroContent heroContent) {
        double conversionRate = heroContent.getConversionRate();

        if (conversionRate >= 10.0) {
            return "EXCELLENT";
        } else if (conversionRate >= 5.0) {
            return "GOOD";
        } else if (conversionRate >= 2.0) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }
}