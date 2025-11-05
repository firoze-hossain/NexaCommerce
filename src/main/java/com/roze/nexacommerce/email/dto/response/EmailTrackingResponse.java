package com.roze.nexacommerce.email.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTrackingResponse {
    private Long id;
    private Long emailLogId;
    private String trackingToken;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private String ipAddress;
    private String userAgent;
    private String country;
    private String city;
    private String clickUrl;
    private Integer clickCount;
    private String deviceType;
    private String browser;
    private String platform;
    private LocalDateTime createdAt;
}