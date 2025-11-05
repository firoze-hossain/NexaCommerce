package com.roze.nexacommerce.email.service;

import com.roze.nexacommerce.email.dto.request.EmailTemplateRequest;
import com.roze.nexacommerce.email.dto.response.EmailTemplateResponse;

import java.util.List;

public interface EmailTemplateService {
    EmailTemplateResponse createTemplate(EmailTemplateRequest request);

    EmailTemplateResponse updateTemplate(Long id, EmailTemplateRequest request);

    EmailTemplateResponse getTemplateById(Long id);

    EmailTemplateResponse getTemplateByKey(String templateKey);

    List<EmailTemplateResponse> getAllTemplates();

    List<EmailTemplateResponse> getActiveTemplates();

    List<EmailTemplateResponse> getTemplatesByCategory(String category);

    void deleteTemplate(Long id);

    EmailTemplateResponse activateTemplate(Long id);

    EmailTemplateResponse deactivateTemplate(Long id);

    EmailTemplateResponse createNewVersion(String templateKey, EmailTemplateRequest request);
}