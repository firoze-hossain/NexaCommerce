package com.roze.nexacommerce.email.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.email.dto.request.EmailConfigurationRequest;
import com.roze.nexacommerce.email.dto.request.EmailTemplateRequest;
import com.roze.nexacommerce.email.dto.request.SendEmailRequest;
import com.roze.nexacommerce.email.dto.request.TestEmailRequest;
import com.roze.nexacommerce.email.dto.response.*;
import com.roze.nexacommerce.email.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController extends BaseController {
    private final EmailConfigurationService emailConfigurationService;
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;
    private final EmailTrackingService emailTrackingService;
    private final EmailAnalyticsService emailAnalyticsService;
    // Email Configuration Endpoints
    @PostMapping("/configurations")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> createConfiguration(
            @Valid @RequestBody EmailConfigurationRequest request) {
        EmailConfigurationResponse response = emailConfigurationService.createConfiguration(request);
        return created(response, "Email configuration created successfully");
    }
    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailAnalyticsResponse>> getEmailAnalytics() {
        EmailAnalyticsResponse response = emailAnalyticsService.getEmailAnalytics();
        return ok(response, "Email analytics retrieved successfully");
    }
    @PutMapping("/configurations/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody EmailConfigurationRequest request) {
        EmailConfigurationResponse response = emailConfigurationService.updateConfiguration(id, request);
        return ok(response, "Email configuration updated successfully");
    }

    @GetMapping("/configurations")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailConfigurationResponse>>> getAllConfigurations() {
        List<EmailConfigurationResponse> responses = emailConfigurationService.getAllConfigurations();
        return ok(responses, "Email configurations retrieved successfully");
    }

    @GetMapping("/configurations/active")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailConfigurationResponse>>> getActiveConfigurations() {
        List<EmailConfigurationResponse> responses = emailConfigurationService.getActiveConfigurations();
        return ok(responses, "Active email configurations retrieved successfully");
    }

    @GetMapping("/configurations/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> getConfigurationById(@PathVariable Long id) {
        EmailConfigurationResponse response = emailConfigurationService.getConfigurationById(id);
        return ok(response, "Email configuration retrieved successfully");
    }

    @GetMapping("/configurations/purpose/{purpose}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> getConfigurationByPurpose(
            @PathVariable String purpose) {
        EmailConfigurationResponse response = emailConfigurationService.getConfigurationByPurpose(purpose);
        return ok(response, "Email configuration retrieved successfully");
    }

    @DeleteMapping("/configurations/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteConfiguration(@PathVariable Long id) {
        emailConfigurationService.deleteConfiguration(id);
        return noContent("Email configuration deleted successfully");
    }

    @PostMapping("/configurations/{id}/activate")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> activateConfiguration(@PathVariable Long id) {
        EmailConfigurationResponse response = emailConfigurationService.activateConfiguration(id);
        return ok(response, "Email configuration activated successfully");
    }

    @PostMapping("/configurations/{id}/default")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailConfigurationResponse>> setAsDefault(@PathVariable Long id) {
        EmailConfigurationResponse response = emailConfigurationService.setAsDefault(id);
        return ok(response, "Email configuration set as default successfully");
    }

    @PostMapping("/configurations/{id}/test")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<TestEmailResponse>> testConfiguration(@PathVariable Long id) {
        TestEmailResponse response = emailConfigurationService.testConfiguration(id);
        return ok(response, "Email configuration test completed");
    }

    @PostMapping("/configurations/{id}/test-email")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<TestEmailResponse>> testConfigurationWithEmail(
            @PathVariable Long id,
            @Valid @RequestBody TestEmailRequest request) {
        TestEmailResponse response = emailConfigurationService.testConfigurationWithEmail(id, request.getTestEmail());
        return ok(response, "Test email sent successfully");
    }

    // Email Template Endpoints
    @PostMapping("/templates")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> createTemplate(
            @Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplateResponse response = emailTemplateService.createTemplate(request);
        return created(response, "Email template created successfully");
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplateResponse response = emailTemplateService.updateTemplate(id, request);
        return ok(response, "Email template updated successfully");
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        List<EmailTemplateResponse> responses = emailTemplateService.getAllTemplates();
        return ok(responses, "Email templates retrieved successfully");
    }

    @GetMapping("/templates/active")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<List<EmailTemplateResponse>>> getActiveTemplates() {
        List<EmailTemplateResponse> responses = emailTemplateService.getActiveTemplates();
        return ok(responses, "Active email templates retrieved successfully");
    }

    @GetMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> getTemplateById(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.getTemplateById(id);
        return ok(response, "Email template retrieved successfully");
    }

    @GetMapping("/templates/key/{templateKey}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> getTemplateByKey(@PathVariable String templateKey) {
        EmailTemplateResponse response = emailTemplateService.getTemplateByKey(templateKey);
        return ok(response, "Email template retrieved successfully");
    }

    @GetMapping("/templates/category/{category}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<List<EmailTemplateResponse>>> getTemplatesByCategory(
            @PathVariable String category) {
        List<EmailTemplateResponse> responses = emailTemplateService.getTemplatesByCategory(category);
        return ok(responses, "Email templates retrieved successfully");
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteTemplate(@PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        return noContent("Email template deleted successfully");
    }

    @PostMapping("/templates/{id}/activate")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> activateTemplate(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.activateTemplate(id);
        return ok(response, "Email template activated successfully");
    }

    @PostMapping("/templates/{id}/deactivate")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> deactivateTemplate(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.deactivateTemplate(id);
        return ok(response, "Email template deactivated successfully");
    }

    @PostMapping("/templates/{templateKey}/versions")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTemplateResponse>> createNewVersion(
            @PathVariable String templateKey,
            @Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplateResponse response = emailTemplateService.createNewVersion(templateKey, request);
        return created(response, "New template version created successfully");
    }

    // Email Sending Endpoints
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<EmailLogResponse>> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailLogResponse response = emailService.sendEmail(request);
        return ok(response, "Email sent successfully");
    }

    @PostMapping("/send/templated")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('VENDOR')")
    public ResponseEntity<BaseResponse<EmailLogResponse>> sendTemplatedEmail(
            @RequestParam String to,
            @RequestParam String templateKey,
            @RequestParam String purpose,
            @RequestBody(required = false) java.util.Map<String, Object> variables) {
        EmailLogResponse response = emailService.sendTemplatedEmail(to, templateKey, variables, purpose);
        return ok(response, "Templated email sent successfully");
    }

    // Email Logs Endpoints
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailLogResponse>>> getEmailLogs() {
        List<EmailLogResponse> responses = emailService.getEmailLogsByStatus(null);
        return ok(responses, "Email logs retrieved successfully");
    }

    @GetMapping("/logs/status/{status}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailLogResponse>>> getEmailLogsByStatus(@PathVariable String status) {
        List<EmailLogResponse> responses = emailService.getEmailLogsByStatus(status);
        return ok(responses, "Email logs retrieved successfully");
    }

    @GetMapping("/logs/purpose/{purpose}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailLogResponse>>> getEmailLogsByPurpose(@PathVariable String purpose) {
        List<EmailLogResponse> responses = emailService.getEmailLogsByPurpose(purpose);
        return ok(responses, "Email logs retrieved successfully");
    }

    @GetMapping("/logs/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailLogResponse>> getEmailLogById(@PathVariable Long id) {
        EmailLogResponse response = emailService.getEmailLogById(id);
        return ok(response, "Email log retrieved successfully");
    }

    @PostMapping("/retry-failed")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> retryFailedEmails() {
        emailService.retryFailedEmails();
        return ok(null,"Failed emails retry process started");
    }

    @GetMapping("/tracking/{trackingToken}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<EmailTrackingResponse>> getTrackingByToken(@PathVariable String trackingToken) {
        EmailTrackingResponse response = emailTrackingService.getTrackingByToken(trackingToken);
        return ok(response, "Email tracking retrieved successfully");
    }

    @GetMapping("/tracking/email-log/{emailLogId}")
    @PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<EmailTrackingResponse>>> getTrackingByEmailLog(@PathVariable Long emailLogId) {
        List<EmailTrackingResponse> responses = emailTrackingService.getTrackingByEmailLog(emailLogId);
        return ok(responses, "Email tracking retrieved successfully");
    }
}