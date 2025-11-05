package com.roze.nexacommerce.email.dto.response;

import com.roze.nexacommerce.email.enums.EmailCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {
    private Long id;
    private String name;
    private String templateKey;
    private EmailCategory category;
    private String subject;
    private String preheader;
    private String htmlContent;
    private String textContent;
    private String language;
    private Boolean isActive;
    private Boolean isSystemTemplate;
    private Integer templateVersion;
    private List<String> supportedPurposes;
    private Map<String, String> availableVariables;
    private Map<String, Object> defaultVariables;
    private EmailConfigurationResponse defaultConfiguration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}