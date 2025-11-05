package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.EmailLog;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Find by status
    List<EmailLog> findByStatus(EmailStatus status);

    Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);

    // Find by purpose
    List<EmailLog> findByPurpose(EmailPurpose purpose);

    Page<EmailLog> findByPurpose(EmailPurpose purpose, Pageable pageable);

    // Find by recipient email
    List<EmailLog> findByRecipientEmail(String recipientEmail);

    Page<EmailLog> findByRecipientEmail(String recipientEmail, Pageable pageable);

    // Find by configuration
    List<EmailLog> findByEmailConfigurationId(Long configurationId);

    // Find by template
    List<EmailLog> findByEmailTemplateId(Long templateId);

    // Find by message ID (provider's message ID)
    Optional<EmailLog> findByMessageId(String messageId);

    // Find by tracking token
    Optional<EmailLog> findByTrackingToken(String trackingToken);

    // Find failed emails with retry count
    List<EmailLog> findByStatusAndRetryCountLessThan(EmailStatus status, Integer maxRetryCount);

    // Find emails within date range
    List<EmailLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<EmailLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find by status and purpose
    List<EmailLog> findByStatusAndPurpose(EmailStatus status, EmailPurpose purpose);

    // Custom queries for analytics
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.status = :status AND el.purpose = :purpose")
    Long countByStatusAndPurpose(@Param("status") EmailStatus status, @Param("purpose") EmailPurpose purpose);

    @Query("SELECT el.purpose, COUNT(el) FROM EmailLog el WHERE el.createdAt BETWEEN :start AND :end GROUP BY el.purpose")
    List<Object[]> countEmailsByPurposeAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT el.status, COUNT(el) FROM EmailLog el WHERE el.createdAt BETWEEN :start AND :end GROUP BY el.status")
    List<Object[]> countEmailsByStatusAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Find emails for retry
    @Query("SELECT el FROM EmailLog el WHERE el.status = 'FAILED' AND el.retryCount < el.emailConfiguration.maxRetries AND el.createdAt > :cutoffTime")
    List<EmailLog> findEmailsForRetry(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Update status by IDs
    @Modifying
    @Query("UPDATE EmailLog el SET el.status = :status, el.updatedAt = CURRENT_TIMESTAMP WHERE el.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") EmailStatus status);

    // Increment retry count
    @Modifying
    @Query("UPDATE EmailLog el SET el.retryCount = el.retryCount + 1, el.updatedAt = CURRENT_TIMESTAMP WHERE el.id = :id")
    void incrementRetryCount(@Param("id") Long id);

    // Mark as delivered
    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'DELIVERED', el.deliveredAt = :deliveredAt WHERE el.messageId = :messageId")
    void markAsDelivered(@Param("messageId") String messageId, @Param("deliveredAt") LocalDateTime deliveredAt);

    // Mark as opened
    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'OPENED', el.openedAt = :openedAt WHERE el.trackingToken = :trackingToken")
    void markAsOpened(@Param("trackingToken") String trackingToken, @Param("openedAt") LocalDateTime openedAt);

    // Statistics queries
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.createdAt >= :since")
    Long countEmailsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.status = 'SENT' AND el.createdAt BETWEEN :start AND :end")
    Long countSentEmailsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(el.retryCount) FROM EmailLog el WHERE el.status = 'SENT' AND el.createdAt BETWEEN :start AND :end")
    Double averageRetryCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}