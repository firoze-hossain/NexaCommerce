package com.roze.nexacommerce.email.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roze.nexacommerce.email.dto.request.EmailConfigurationRequest;
import com.roze.nexacommerce.email.dto.request.EmailTemplateRequest;
import com.roze.nexacommerce.email.dto.response.EmailConfigurationResponse;
import com.roze.nexacommerce.email.dto.response.EmailLogResponse;
import com.roze.nexacommerce.email.dto.response.EmailTemplateResponse;
import com.roze.nexacommerce.email.dto.response.EmailTrackingResponse;
import com.roze.nexacommerce.email.entity.EmailConfiguration;
import com.roze.nexacommerce.email.entity.EmailLog;
import com.roze.nexacommerce.email.entity.EmailTemplate;
import com.roze.nexacommerce.email.entity.EmailTracking;
import com.roze.nexacommerce.email.repository.EmailConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmailMapper {
    private final ModelMapper modelMapper;
    private final EmailConfigurationRepository emailConfigurationRepository;
    private final ObjectMapper objectMapper;
    // EmailConfiguration Mappings
    public EmailConfiguration toEntity(EmailConfigurationRequest request) {
        EmailConfiguration configuration = modelMapper.map(request, EmailConfiguration.class);
        if (configuration.getTemplateData() != null && configuration.getTemplateData().trim().isEmpty()) {
            configuration.setTemplateData(null);
        }

        // Set default values if not provided
        if (configuration.getUseTls() == null) {
            configuration.setUseTls(true);
        }
        if (configuration.getUseSsl() == null) {
            configuration.setUseSsl(false);
        }
        if (configuration.getIsActive() == null) {
            configuration.setIsActive(true);
        }
        if (configuration.getIsDefault() == null) {
            configuration.setIsDefault(false);
        }
        if (configuration.getMaxRetries() == null) {
            configuration.setMaxRetries(3);
        }
        if (configuration.getTimeoutMs() == null) {
            configuration.setTimeoutMs(30000);
        }
        if (configuration.getRateLimitPerHour() == null) {
            configuration.setRateLimitPerHour(1000);
        }
        
        return configuration;
    }

    public EmailConfigurationResponse toResponse(EmailConfiguration configuration) {
        return modelMapper.map(configuration, EmailConfigurationResponse.class);
    }

    public List<EmailConfigurationResponse> toConfigurationResponseList(List<EmailConfiguration> configurations) {
        return configurations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // EmailTemplate Mappings
    public EmailTemplate toEntity(EmailTemplateRequest request) {
        EmailTemplate template = modelMapper.map(request, EmailTemplate.class);
        
        // Set default configuration if provided
        if (request.getDefaultConfigurationId() != null) {
            emailConfigurationRepository.findById(request.getDefaultConfigurationId())
                    .ifPresent(template::setDefaultConfiguration);
        }
        
        // Set default values
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }
        if (template.getIsSystemTemplate() == null) {
            template.setIsSystemTemplate(false);
        }
        if (template.getTemplateVersion() == null) {
            template.setTemplateVersion(1);
        }
        return template;
    }

    public EmailTemplateResponse toResponse(EmailTemplate template) {
        EmailTemplateResponse response = modelMapper.map(template, EmailTemplateResponse.class);
        
        // Map default configuration
        if (template.getDefaultConfiguration() != null) {
            response.setDefaultConfiguration(toResponse(template.getDefaultConfiguration()));
        }
        
        return response;
    }

    public List<EmailTemplateResponse> toTemplateResponseList(List<EmailTemplate> templates) {
        return templates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // EmailLog Mappings
    public EmailLogResponse toResponse(EmailLog emailLog) {
        EmailLogResponse response = modelMapper.map(emailLog, EmailLogResponse.class);
        
        // Map related entities
        if (emailLog.getEmailConfiguration() != null) {
            response.setEmailConfiguration(toResponse(emailLog.getEmailConfiguration()));
        }
        if (emailLog.getEmailTemplate() != null) {
            response.setEmailTemplate(toResponse(emailLog.getEmailTemplate()));
        }
        
        return response;
    }

    public List<EmailLogResponse> toLogResponseList(List<EmailLog> emailLogs) {
        return emailLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // EmailTracking Mappings
    public EmailTrackingResponse toResponse(EmailTracking tracking) {
        return modelMapper.map(tracking, EmailTrackingResponse.class);
    }

    public List<EmailTrackingResponse> toTrackingResponseList(List<EmailTracking> trackings) {
        return trackings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Update methods
    public void updateConfigurationFromRequest(EmailConfigurationRequest request, EmailConfiguration configuration) {
        modelMapper.map(request, configuration);
    }

    public void updateTemplateFromRequest(EmailTemplateRequest request, EmailTemplate template) {
        modelMapper.map(request, template);
        
        // Update default configuration if provided
        if (request.getDefaultConfigurationId() != null) {
            emailConfigurationRepository.findById(request.getDefaultConfigurationId())
                    .ifPresent(template::setDefaultConfiguration);
        } else if (request.getDefaultConfigurationId() == null) {
            template.setDefaultConfiguration(null);
        }
    }
}