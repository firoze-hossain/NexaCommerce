package com.roze.nexacommerce.email.service.impl;

import com.roze.nexacommerce.email.dto.request.EmailConfigurationRequest;
import com.roze.nexacommerce.email.dto.response.EmailConfigurationResponse;
import com.roze.nexacommerce.email.dto.response.TestEmailResponse;
import com.roze.nexacommerce.email.entity.EmailConfiguration;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.mapper.EmailMapper;
import com.roze.nexacommerce.email.repository.EmailConfigurationRepository;
import com.roze.nexacommerce.email.service.EmailConfigurationService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfigurationServiceImpl implements EmailConfigurationService {
    private final EmailConfigurationRepository emailConfigurationRepository;
    private final EmailMapper emailMapper;

    @Override
    @Transactional
    public EmailConfigurationResponse createConfiguration(EmailConfigurationRequest request) {
        log.info("Creating new email configuration: {}", request.getConfigName());
        
        EmailConfiguration configuration = emailMapper.toEntity(request);
        EmailConfiguration savedConfiguration = emailConfigurationRepository.save(configuration);
        
        log.info("Successfully created email configuration with ID: {}", savedConfiguration.getId());
        return emailMapper.toResponse(savedConfiguration);
    }

    @Override
    @Transactional
    public EmailConfigurationResponse updateConfiguration(Long id, EmailConfigurationRequest request) {
        log.info("Updating email configuration with ID: {}", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        emailMapper.updateConfigurationFromRequest(request, configuration);
        EmailConfiguration updatedConfiguration = emailConfigurationRepository.save(configuration);
        
        log.info("Successfully updated email configuration with ID: {}", id);
        return emailMapper.toResponse(updatedConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailConfigurationResponse getConfigurationById(Long id) {
        log.debug("Fetching email configuration with ID: {}", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        return emailMapper.toResponse(configuration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailConfigurationResponse> getAllConfigurations() {
        log.debug("Fetching all email configurations");
        
        List<EmailConfiguration> configurations = emailConfigurationRepository.findAll();
        return emailMapper.toConfigurationResponseList(configurations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailConfigurationResponse> getActiveConfigurations() {
        log.debug("Fetching active email configurations");
        
        List<EmailConfiguration> configurations = emailConfigurationRepository.findByIsActiveTrue();
        return emailMapper.toConfigurationResponseList(configurations);
    }

    @Override
    @Transactional
    public void deleteConfiguration(Long id) {
        log.info("Deleting email configuration with ID: {}", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        emailConfigurationRepository.delete(configuration);
        log.info("Successfully deleted email configuration with ID: {}", id);
    }

    @Override
    @Transactional
    public EmailConfigurationResponse activateConfiguration(Long id) {
        log.info("Activating email configuration with ID: {}", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        configuration.setIsActive(true);
        EmailConfiguration updatedConfiguration = emailConfigurationRepository.save(configuration);
        
        log.info("Successfully activated email configuration with ID: {}", id);
        return emailMapper.toResponse(updatedConfiguration);
    }

    @Override
    @Transactional
    public EmailConfigurationResponse setAsDefault(Long id) {
        log.info("Setting email configuration with ID: {} as default", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        // Deactivate other defaults for the same purpose
        emailConfigurationRepository.updateDefaultConfiguration(configuration.getPurpose(), id);
        
        configuration.setIsDefault(true);
        EmailConfiguration updatedConfiguration = emailConfigurationRepository.save(configuration);
        
        log.info("Successfully set email configuration with ID: {} as default", id);
        return emailMapper.toResponse(updatedConfiguration);
    }

    @Override
    @Transactional
    public TestEmailResponse testConfiguration(Long id) {
        log.info("Testing email configuration with ID: {}", id);
        
        EmailConfiguration configuration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", id));
        
        // Implementation for testing email configuration
        // This would actually send a test email
        boolean success = true; // Placeholder
        String message = success ? "Test email sent successfully" : "Failed to send test email";
        
        return TestEmailResponse.builder()
                .success(success)
                .message(message)
                .testedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public TestEmailResponse testConfigurationWithEmail(Long id, String testEmail) {
        log.info("Testing email configuration with ID: {} using email: {}", id, testEmail);
        
        // Similar to testConfiguration but with specific email
        boolean success = true; // Placeholder
        String message = success ? "Test email sent successfully to " + testEmail : "Failed to send test email";
        
        return TestEmailResponse.builder()
                .success(success)
                .message(message)
                .testedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailConfigurationResponse getConfigurationByPurpose(String purpose) {
        log.debug("Fetching email configuration for purpose: {}", purpose);
        
        EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
        EmailConfiguration configuration = emailConfigurationRepository.findByPurposeAndIsActiveTrue(emailPurpose)
                .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", purpose));
        
        return emailMapper.toResponse(configuration);
    }
}