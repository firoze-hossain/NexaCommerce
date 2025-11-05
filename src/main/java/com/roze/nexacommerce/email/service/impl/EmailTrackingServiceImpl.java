package com.roze.nexacommerce.email.service.impl;

import com.roze.nexacommerce.email.dto.response.EmailTrackingResponse;
import com.roze.nexacommerce.email.entity.EmailLog;
import com.roze.nexacommerce.email.entity.EmailTracking;
import com.roze.nexacommerce.email.mapper.EmailMapper;
import com.roze.nexacommerce.email.repository.EmailLogRepository;
import com.roze.nexacommerce.email.repository.EmailTrackingRepository;
import com.roze.nexacommerce.email.service.EmailTrackingService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTrackingServiceImpl implements EmailTrackingService {
    
    private final EmailTrackingRepository emailTrackingRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailMapper emailMapper;
    
    @Override
    @Transactional
    public void trackEmailOpen(String trackingToken, String ipAddress, String userAgent) {
        log.debug("Tracking email open for token: {}", trackingToken);
        
        EmailLog emailLog = emailLogRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "trackingToken", trackingToken));
        
        EmailTracking tracking = emailTrackingRepository.findByTrackingToken(trackingToken)
                .orElseGet(() -> createNewTracking(emailLog, trackingToken, ipAddress, userAgent));
        
        if (tracking.getOpenedAt() == null) {
            tracking.setOpenedAt(LocalDateTime.now());
            tracking.setIpAddress(ipAddress);
            tracking.setUserAgent(userAgent);
            
            // Update email log status
            emailLog.setStatus(com.roze.nexacommerce.email.enums.EmailStatus.OPENED);
            emailLog.setOpenedAt(LocalDateTime.now());
            
            emailTrackingRepository.save(tracking);
            emailLogRepository.save(emailLog);
            
            log.info("Email opened tracked for token: {}", trackingToken);
        }
    }
    
    @Override
    @Transactional
    public void trackEmailClick(String trackingToken, String clickUrl, String ipAddress, String userAgent) {
        log.debug("Tracking email click for token: {}, URL: {}", trackingToken, clickUrl);
        
        EmailLog emailLog = emailLogRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "trackingToken", trackingToken));
        
        EmailTracking tracking = emailTrackingRepository.findByTrackingToken(trackingToken)
                .orElseGet(() -> createNewTracking(emailLog, trackingToken, ipAddress, userAgent));
        
        tracking.setClickedAt(LocalDateTime.now());
        tracking.setClickUrl(clickUrl);
        tracking.setClickCount(tracking.getClickCount() != null ? tracking.getClickCount() + 1 : 1);
        tracking.setIpAddress(ipAddress);
        tracking.setUserAgent(userAgent);
        
        // Update email log status
        emailLog.setStatus(com.roze.nexacommerce.email.enums.EmailStatus.CLICKED);
        
        emailTrackingRepository.save(tracking);
        emailLogRepository.save(emailLog);
        
        log.info("Email click tracked for token: {}, URL: {}", trackingToken, clickUrl);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmailTrackingResponse getTrackingByToken(String trackingToken) {
        log.debug("Fetching tracking by token: {}", trackingToken);
        
        EmailTracking tracking = emailTrackingRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTracking", "trackingToken", trackingToken));
        
        return emailMapper.toResponse(tracking);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmailTrackingResponse> getTrackingByEmailLog(Long emailLogId) {
        log.debug("Fetching tracking for email log ID: {}", emailLogId);
        
        List<EmailTracking> trackings = emailTrackingRepository.findByEmailLogId(emailLogId);
        return emailMapper.toTrackingResponseList(trackings);
    }
    
    private EmailTracking createNewTracking(EmailLog emailLog, String trackingToken, String ipAddress, String userAgent) {
        return EmailTracking.builder()
                .emailLog(emailLog)
                .trackingToken(trackingToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .clickCount(0)
                .build();
    }
}