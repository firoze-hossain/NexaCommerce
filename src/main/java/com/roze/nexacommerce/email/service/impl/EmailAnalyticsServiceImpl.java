package com.roze.nexacommerce.email.service.impl;

import com.roze.nexacommerce.email.dto.response.EmailAnalyticsResponse;
import com.roze.nexacommerce.email.entity.EmailLog;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.enums.EmailStatus;
import com.roze.nexacommerce.email.repository.EmailLogRepository;
import com.roze.nexacommerce.email.repository.EmailTrackingRepository;
import com.roze.nexacommerce.email.service.EmailAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAnalyticsServiceImpl implements EmailAnalyticsService {

    private final EmailLogRepository emailLogRepository;
    private final EmailTrackingRepository emailTrackingRepository;

    @Override
    @Transactional(readOnly = true)
    public EmailAnalyticsResponse getEmailAnalytics() {
        log.debug("Fetching email analytics");

        // Get all email logs for analytics
        List<EmailLog> allEmailLogs = emailLogRepository.findAll();
        
        return buildAnalyticsResponse(allEmailLogs, "Overall Analytics");
    }

    @Override
    @Transactional(readOnly = true)
    public EmailAnalyticsResponse getEmailAnalyticsByPurpose(String purpose) {
        log.debug("Fetching email analytics for purpose: {}", purpose);

        EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
        List<EmailLog> emailLogs = emailLogRepository.findByPurpose(emailPurpose);
        
        return buildAnalyticsResponse(emailLogs, "Analytics for " + purpose);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailAnalyticsResponse getEmailAnalyticsForPeriod(String startDate, String endDate) {
        log.debug("Fetching email analytics for period: {} to {}", startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);

        List<EmailLog> emailLogs = emailLogRepository.findByCreatedAtBetween(start, end);
        
        return buildAnalyticsResponse(emailLogs, "Analytics for period " + startDate + " to " + endDate);
    }

    private EmailAnalyticsResponse buildAnalyticsResponse(List<EmailLog> emailLogs, String description) {
        if (emailLogs.isEmpty()) {
            return createEmptyAnalyticsResponse(description);
        }

        long totalEmails = emailLogs.size();
        long sentEmails = countByStatus(emailLogs, EmailStatus.SENT);
        long deliveredEmails = countByStatus(emailLogs, EmailStatus.DELIVERED);
        long openedEmails = countByStatus(emailLogs, EmailStatus.OPENED);
        long clickedEmails = countByStatus(emailLogs, EmailStatus.CLICKED);
        long failedEmails = countByStatus(emailLogs, EmailStatus.FAILED);

        double deliveryRate = totalEmails > 0 ? (double) deliveredEmails / totalEmails * 100 : 0;
        double openRate = deliveredEmails > 0 ? (double) openedEmails / deliveredEmails * 100 : 0;
        double clickRate = openedEmails > 0 ? (double) clickedEmails / openedEmails * 100 : 0;

        Map<String, Long> emailsByPurpose = emailLogs.stream()
                .collect(Collectors.groupingBy(
                    log -> log.getPurpose().name(),
                    Collectors.counting()
                ));

        Map<String, Long> emailsByStatus = emailLogs.stream()
                .collect(Collectors.groupingBy(
                    log -> log.getStatus().name(),
                    Collectors.counting()
                ));

        // Get tracking data for opens by device and clicks by URL
        Map<String, Long> opensByDevice = getOpensByDevice();
        Map<String, Long> clicksByUrl = getClicksByUrl();

        return EmailAnalyticsResponse.builder()
                .totalEmails(totalEmails)
                .sentEmails(sentEmails)
                .deliveredEmails(deliveredEmails)
                .openedEmails(openedEmails)
                .clickedEmails(clickedEmails)
                .failedEmails(failedEmails)
                .deliveryRate(Math.round(deliveryRate * 10.0) / 10.0) // Round to 1 decimal
                .openRate(Math.round(openRate * 10.0) / 10.0)
                .clickRate(Math.round(clickRate * 10.0) / 10.0)
                .emailsByPurpose(emailsByPurpose)
                .emailsByStatus(emailsByStatus)
                .opensByDevice(opensByDevice)
                .clicksByUrl(clicksByUrl)
                .build();
    }

    private long countByStatus(List<EmailLog> emailLogs, EmailStatus status) {
        return emailLogs.stream()
                .filter(log -> log.getStatus() == status)
                .count();
    }

    private Map<String, Long> getOpensByDevice() {
        // This would typically query your tracking repository for device information
        // For now, return empty map or mock data
        return Map.of(
            "DESKTOP", 150L,
            "MOBILE", 85L,
            "TABLET", 15L
        );
    }

    private Map<String, Long> getClicksByUrl() {
        // This would typically query your tracking repository for click URLs
        // For now, return empty map or mock data
        return Map.of(
            "https://example.com/products", 45L,
            "https://example.com/promotions", 30L,
            "https://example.com/support", 15L
        );
    }

    private EmailAnalyticsResponse createEmptyAnalyticsResponse(String description) {
        return EmailAnalyticsResponse.builder()
                .totalEmails(0L)
                .sentEmails(0L)
                .deliveredEmails(0L)
                .openedEmails(0L)
                .clickedEmails(0L)
                .failedEmails(0L)
                .deliveryRate(0.0)
                .openRate(0.0)
                .clickRate(0.0)
                .emailsByPurpose(Map.of())
                .emailsByStatus(Map.of())
                .opensByDevice(Map.of())
                .clicksByUrl(Map.of())
                .build();
    }
}