package com.roze.nexacommerce.email.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAnalyticsResponse {
    private Long totalEmails;
    private Long sentEmails;
    private Long deliveredEmails;
    private Long openedEmails;
    private Long clickedEmails;
    private Long failedEmails;
    private Double deliveryRate;
    private Double openRate;
    private Double clickRate;
    private Map<String, Long> emailsByPurpose;
    private Map<String, Long> emailsByStatus;
    private Map<String, Long> opensByDevice;
    private Map<String, Long> clicksByUrl;
}