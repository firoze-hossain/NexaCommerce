package com.roze.nexacommerce.email.dto.request;

import com.roze.nexacommerce.email.enums.EmailCategory;
import com.roze.nexacommerce.email.enums.EmailProvider;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfigurationRequest {
    @NotBlank(message = "Configuration name is required")
    private String configName;
    
    @NotNull(message = "Email purpose is required")
    private EmailPurpose purpose;
    
    @NotBlank(message = "From email is required")
    @Email(message = "From email must be valid")
    private String fromEmail;
    
    private String fromName;
    
    @NotNull(message = "Email provider is required")
    private EmailProvider provider;
    
    // SMTP Configuration
    private String host;
    
    @Positive(message = "Port must be positive")
    private Integer port;
    
    private String username;
    private String password;
    
    // Advanced Settings
    private Boolean useTls;
    private Boolean useSsl;
    private String encryptionType;
    
    // Provider-specific
    private String apiKey;
    private String apiSecret;
    private String domain;
    private String region;
    
    // Configuration
    private Boolean isActive;
    private Boolean isDefault;
    
    private Integer maxRetries;
    private Integer timeoutMs;
    private Integer rateLimitPerHour;
    
    private String templateId;
    private String templateData;
    private String customHeaders;
}