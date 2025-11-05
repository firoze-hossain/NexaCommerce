package com.roze.nexacommerce.email.dto.response;

import com.roze.nexacommerce.email.enums.EmailProvider;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfigurationResponse {
    private Long id;
    private String configName;
    private EmailPurpose purpose;
    private String fromEmail;
    private String fromName;
    private EmailProvider provider;
    private String host;
    private Integer port;
    private String username;
    private Boolean useTls;
    private Boolean useSsl;
    private String encryptionType;
    private String domain;
    private String region;
    private Boolean isActive;
    private Boolean isDefault;
    private Integer maxRetries;
    private Integer timeoutMs;
    private Integer rateLimitPerHour;
    private String templateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}