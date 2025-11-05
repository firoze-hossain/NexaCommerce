package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.EmailTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTrackingRepository extends JpaRepository<EmailTracking, Long> {

    // Find tracking by email log
    List<EmailTracking> findByEmailLogId(Long emailLogId);

    Optional<EmailTracking> findByTrackingToken(String trackingToken);

    // Find opened emails
    List<EmailTracking> findByOpenedAtIsNotNull();

    Page<EmailTracking> findByOpenedAtIsNotNull(Pageable pageable);

    // Find clicked emails
    List<EmailTracking> findByClickedAtIsNotNull();

    // Find tracking by IP address
    List<EmailTracking> findByIpAddress(String ipAddress);

    // Find tracking within date range
    List<EmailTracking> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Analytics queries
    @Query("SELECT COUNT(et) FROM EmailTracking et WHERE et.openedAt IS NOT NULL AND et.emailLog.createdAt BETWEEN :start AND :end")
    Long countOpenedEmailsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(et) FROM EmailTracking et WHERE et.clickedAt IS NOT NULL AND et.emailLog.createdAt BETWEEN :start AND :end")
    Long countClickedEmailsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT et.deviceType, COUNT(et) FROM EmailTracking et WHERE et.openedAt IS NOT NULL GROUP BY et.deviceType")
    List<Object[]> countOpensByDeviceType();

    @Query("SELECT et.country, COUNT(et) FROM EmailTracking et WHERE et.openedAt IS NOT NULL GROUP BY et.country")
    List<Object[]> countOpensByCountry();

    @Query("SELECT et.browser, COUNT(et) FROM EmailTracking et WHERE et.openedAt IS NOT NULL GROUP BY et.browser")
    List<Object[]> countOpensByBrowser();

    // Click-through rate by purpose
    @Query("SELECT et.emailLog.purpose, COUNT(et) FROM EmailTracking et WHERE et.clickedAt IS NOT NULL AND et.emailLog.createdAt BETWEEN :start AND :end GROUP BY et.emailLog.purpose")
    List<Object[]> countClicksByPurpose(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Most clicked URLs
    @Query("SELECT et.clickUrl, COUNT(et) FROM EmailTracking et WHERE et.clickedAt IS NOT NULL GROUP BY et.clickUrl ORDER BY COUNT(et) DESC")
    List<Object[]> findMostClickedUrls();

    // Update click count
    @Query("UPDATE EmailTracking et SET et.clickCount = et.clickCount + 1, et.clickedAt = CURRENT_TIMESTAMP WHERE et.id = :id")
    void incrementClickCount(@Param("id") Long id);

    // Find duplicate tracking (for cleanup)
    @Query("SELECT et.trackingToken, COUNT(et) FROM EmailTracking et GROUP BY et.trackingToken HAVING COUNT(et) > 1")
    List<Object[]> findDuplicateTrackingTokens();
}