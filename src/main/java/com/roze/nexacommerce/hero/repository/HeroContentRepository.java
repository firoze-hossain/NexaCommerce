
package com.roze.nexacommerce.hero.repository;

import com.roze.nexacommerce.hero.entity.HeroContent;
import com.roze.nexacommerce.hero.enums.HeroType;
import com.roze.nexacommerce.hero.enums.TargetAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HeroContentRepository extends JpaRepository<HeroContent, Long> {

    @Query("SELECT h FROM HeroContent h WHERE h.active = true AND h.startDate <= :now AND (h.endDate IS NULL OR h.endDate >= :now) ORDER BY h.displayOrder DESC")
    List<HeroContent> findActiveContent(@Param("now") LocalDateTime now);

    List<HeroContent> findByTypeAndActiveTrueOrderByDisplayOrderDesc(HeroType type);

    List<HeroContent> findByTargetAudienceAndActiveTrueOrderByDisplayOrderDesc(TargetAudience targetAudience);

    List<HeroContent> findByTypeAndTargetAudienceAndActiveTrueOrderByDisplayOrderDesc(
            HeroType type, TargetAudience targetAudience);

    @Query("SELECT h FROM HeroContent h WHERE h.active = true AND h.type = :type AND h.startDate <= :now AND (h.endDate IS NULL OR h.endDate >= :now) ORDER BY h.displayOrder DESC")
    List<HeroContent> findActiveByType(@Param("type") HeroType type, @Param("now") LocalDateTime now);

    @Query("SELECT h FROM HeroContent h WHERE h.active = true AND h.targetAudience = :audience AND h.startDate <= :now AND (h.endDate IS NULL OR h.endDate >= :now) ORDER BY h.displayOrder DESC")
    List<HeroContent> findActiveByAudience(@Param("audience") TargetAudience audience, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE HeroContent h SET h.active = false WHERE h.endDate < :now")
    int deactivateExpiredContent(@Param("now") LocalDateTime now);

    @Query("SELECT h FROM HeroContent h WHERE h.active = true ORDER BY h.impressions DESC LIMIT 5")
    List<HeroContent> findTopPerformingContent();

    boolean existsByTitleAndActiveTrue(String title);
}