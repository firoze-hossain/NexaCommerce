package com.roze.nexacommerce.email.service;

import com.roze.nexacommerce.email.dto.response.EmailTrackingResponse;

import java.util.List;

public interface EmailTrackingService {
    void trackEmailOpen(String trackingToken, String ipAddress, String userAgent);

    void trackEmailClick(String trackingToken, String clickUrl, String ipAddress, String userAgent);

    EmailTrackingResponse getTrackingByToken(String trackingToken);

    List<EmailTrackingResponse> getTrackingByEmailLog(Long emailLogId);
}