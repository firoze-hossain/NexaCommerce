package com.roze.nexacommerce.email.dto.request;

import com.roze.nexacommerce.email.enums.EmailCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateRequest {
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotBlank(message = "Template key is required")
    private String templateKey;
    
    @NotNull(message = "Category is required")
    private EmailCategory category;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String preheader;
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;
    
    private String textContent;
    
    private String language;
    
    private Boolean isActive;
    private Boolean isSystemTemplate;
    
    private List<String> supportedPurposes;
    private Map<String, String> availableVariables;
    private Map<String, Object> defaultVariables;
    
    private Long defaultConfigurationId;
}