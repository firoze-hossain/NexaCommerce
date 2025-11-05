package com.roze.nexacommerce.email.dto.response;

import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLogResponse {
    private Long id;
    private EmailConfigurationResponse emailConfiguration;
    private EmailTemplateResponse emailTemplate;
    private EmailPurpose purpose;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String content;
    private EmailStatus status;
    private String messageId;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private String errorMessage;
    private Integer retryCount;
    private String trackingToken;
    private Boolean isTracked;
    private LocalDateTime createdAt;
}