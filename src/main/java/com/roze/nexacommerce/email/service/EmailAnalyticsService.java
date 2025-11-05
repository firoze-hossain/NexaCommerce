package com.roze.nexacommerce.email.service;

import com.roze.nexacommerce.email.dto.response.EmailAnalyticsResponse;

public interface EmailAnalyticsService {
    EmailAnalyticsResponse getEmailAnalytics();

    EmailAnalyticsResponse getEmailAnalyticsByPurpose(String purpose);

    EmailAnalyticsResponse getEmailAnalyticsForPeriod(String startDate, String endDate);
}