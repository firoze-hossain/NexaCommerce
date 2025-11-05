package com.roze.nexacommerce.email.service;

import com.roze.nexacommerce.email.dto.request.SendEmailRequest;
import com.roze.nexacommerce.email.dto.response.EmailLogResponse;

import java.util.List;
import java.util.Map;

public interface EmailService {
    EmailLogResponse sendEmail(SendEmailRequest request);

    EmailLogResponse sendTemplatedEmail(String to, String templateKey, Map<String, Object> variables, String purpose);

    List<EmailLogResponse> sendBulkEmails(List<String> recipients, String templateKey, Map<String, Object> variables, String purpose);

    void retryFailedEmails();

    List<EmailLogResponse> getEmailLogsByStatus(String status);

    List<EmailLogResponse> getEmailLogsByPurpose(String purpose);

    EmailLogResponse getEmailLogById(Long id);
}