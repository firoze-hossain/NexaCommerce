package com.roze.nexacommerce.email.service.impl;

import com.roze.nexacommerce.email.dto.request.EmailTemplateRequest;
import com.roze.nexacommerce.email.dto.response.EmailTemplateResponse;
import com.roze.nexacommerce.email.entity.EmailTemplate;
import com.roze.nexacommerce.email.enums.EmailCategory;
import com.roze.nexacommerce.email.mapper.EmailMapper;
import com.roze.nexacommerce.email.repository.EmailTemplateRepository;
import com.roze.nexacommerce.email.service.EmailTemplateService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailMapper emailMapper;

    @Override
    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request) {
        log.info("Creating new email template: {}", request.getName());
        
        // Check if template key already exists
        if (emailTemplateRepository.existsByTemplateKey(request.getTemplateKey())) {
            throw new IllegalArgumentException("Template key already exists: " + request.getTemplateKey());
        }
        
        EmailTemplate template = emailMapper.toEntity(request);
        EmailTemplate savedTemplate = emailTemplateRepository.save(template);
        
        log.info("Successfully created email template with ID: {}", savedTemplate.getId());
        return emailMapper.toResponse(savedTemplate);
    }

    @Override
    @Transactional
    public EmailTemplateResponse updateTemplate(Long id, EmailTemplateRequest request) {
        log.info("Updating email template with ID: {}", id);
        
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        
        // Check if template key is being changed and if it already exists
        if (!template.getTemplateKey().equals(request.getTemplateKey()) && 
            emailTemplateRepository.existsByTemplateKey(request.getTemplateKey())) {
            throw new IllegalArgumentException("Template key already exists: " + request.getTemplateKey());
        }
        
        emailMapper.updateTemplateFromRequest(request, template);
        EmailTemplate updatedTemplate = emailTemplateRepository.save(template);
        
        log.info("Successfully updated email template with ID: {}", id);
        return emailMapper.toResponse(updatedTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateById(Long id) {
        log.debug("Fetching email template with ID: {}", id);
        
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        
        return emailMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateByKey(String templateKey) {
        log.debug("Fetching email template with key: {}", templateKey);
        
        EmailTemplate template = emailTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));
        
        return emailMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllTemplates() {
        log.debug("Fetching all email templates");
        
        List<EmailTemplate> templates = emailTemplateRepository.findAll();
        return emailMapper.toTemplateResponseList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getActiveTemplates() {
        log.debug("Fetching active email templates");
        
        List<EmailTemplate> templates = emailTemplateRepository.findByIsActiveTrue();
        return emailMapper.toTemplateResponseList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getTemplatesByCategory(String category) {
        log.debug("Fetching email templates for category: {}", category);
        
        EmailCategory emailCategory = EmailCategory.valueOf(category.toUpperCase());
        List<EmailTemplate> templates = emailTemplateRepository.findByCategoryAndIsActiveTrue(emailCategory);
        return emailMapper.toTemplateResponseList(templates);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("Deleting email template with ID: {}", id);
        
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        
        emailTemplateRepository.delete(template);
        log.info("Successfully deleted email template with ID: {}", id);
    }

    @Override
    @Transactional
    public EmailTemplateResponse activateTemplate(Long id) {
        log.info("Activating email template with ID: {}", id);
        
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        
        template.setIsActive(true);
        EmailTemplate updatedTemplate = emailTemplateRepository.save(template);
        
        log.info("Successfully activated email template with ID: {}", id);
        return emailMapper.toResponse(updatedTemplate);
    }

    @Override
    @Transactional
    public EmailTemplateResponse deactivateTemplate(Long id) {
        log.info("Deactivating email template with ID: {}", id);
        
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        
        template.setIsActive(false);
        EmailTemplate updatedTemplate = emailTemplateRepository.save(template);
        
        log.info("Successfully deactivated email template with ID: {}", id);
        return emailMapper.toResponse(updatedTemplate);
    }

    @Override
    @Transactional
    public EmailTemplateResponse createNewVersion(String templateKey, EmailTemplateRequest request) {
        log.info("Creating new version for template: {}", templateKey);
        
        EmailTemplate existingTemplate = emailTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));
        
        // Deactivate old versions
        emailTemplateRepository.deactivateOldTemplateVersions(templateKey, existingTemplate.getTemplateVersion() + 1);
        
        // Create new version
        EmailTemplate newVersion = emailMapper.toEntity(request);
        newVersion.setTemplateKey(templateKey);
        newVersion.setTemplateVersion(existingTemplate.getTemplateVersion() + 1);
        newVersion.setIsSystemTemplate(existingTemplate.getIsSystemTemplate());
        
        EmailTemplate savedTemplate = emailTemplateRepository.save(newVersion);
        
        log.info("Successfully created new version {} for template: {}", savedTemplate.getVersion(), templateKey);
        return emailMapper.toResponse(savedTemplate);
    }
}