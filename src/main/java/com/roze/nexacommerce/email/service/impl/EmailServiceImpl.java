//package com.roze.nexacommerce.email.service.impl;
//
//import com.roze.nexacommerce.email.dto.request.SendEmailRequest;
//import com.roze.nexacommerce.email.dto.response.EmailLogResponse;
//import com.roze.nexacommerce.email.entity.EmailAttachment;
//import com.roze.nexacommerce.email.entity.EmailConfiguration;
//import com.roze.nexacommerce.email.entity.EmailLog;
//import com.roze.nexacommerce.email.entity.EmailTemplate;
//import com.roze.nexacommerce.email.enums.EmailPurpose;
//import com.roze.nexacommerce.email.enums.EmailStatus;
//import com.roze.nexacommerce.email.mapper.EmailMapper;
//import com.roze.nexacommerce.email.repository.EmailAttachmentRepository;
//import com.roze.nexacommerce.email.repository.EmailConfigurationRepository;
//import com.roze.nexacommerce.email.repository.EmailLogRepository;
//import com.roze.nexacommerce.email.repository.EmailTemplateRepository;
//import com.roze.nexacommerce.email.service.EmailService;
//import com.roze.nexacommerce.exception.ResourceNotFoundException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailServiceImpl implements EmailService {
//
//    private final EmailConfigurationRepository emailConfigurationRepository;
//    private final EmailTemplateRepository emailTemplateRepository;
//    private final EmailLogRepository emailLogRepository;
//    private final EmailMapper emailMapper;
//    private final EmailAttachmentRepository emailAttachmentRepository;
//
//    @Override
//    @Transactional
//    public EmailLogResponse sendEmail(SendEmailRequest request) {
//        log.info("Sending email to: {} with purpose: {}", request.getTo(), request.getPurpose());
//
//        try {
//            // Get or create email configuration
//            EmailConfiguration configuration = getEmailConfiguration(request);
//
//            // Create email log
//            EmailLog emailLog = createEmailLog(request, configuration, null);
//
//            // Send email
//            boolean sent = sendEmailWithConfiguration(configuration, request, emailLog);
//
//            // Update email log status
//            if (sent) {
//                emailLog.setStatus(EmailStatus.SENT);
//                emailLog.setSentAt(LocalDateTime.now());
//                log.info("Email sent successfully to: {}, Message ID: {}", request.getTo(), emailLog.getMessageId());
//            } else {
//                emailLog.setStatus(EmailStatus.FAILED);
//                emailLog.setErrorMessage("Failed to send email");
//                log.error("Failed to send email to: {}", request.getTo());
//            }
//
//            EmailLog savedLog = emailLogRepository.save(emailLog);
//            return emailMapper.toResponse(savedLog);
//
//        } catch (Exception e) {
//            log.error("Error sending email to: {}", request.getTo(), e);
//            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    @Transactional
//    public EmailLogResponse sendTemplatedEmail(String to, String templateKey, Map<String, Object> variables, String purpose) {
//        log.info("Sending templated email to: {} with template: {} and purpose: {}", to, templateKey, purpose);
//
//        try {
//            // Get template
//            EmailTemplate template = emailTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey)
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));
//
//            // Get configuration
//            EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
//            EmailConfiguration configuration = emailConfigurationRepository
//                    .findByPurposeAndIsActiveTrue(emailPurpose)
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", purpose));
//
//            // Process template
//            String subject = processTemplate(template.getSubject(), variables);
//            String htmlContent = processTemplate(template.getHtmlContent(), variables);
//            String textContent = template.getTextContent() != null ?
//                    processTemplate(template.getTextContent(), variables) : null;
//
//            // Create send request
//            SendEmailRequest sendRequest = SendEmailRequest.builder()
//                    .to(to)
//                    .subject(subject)
//                    .content(htmlContent)
//                    .purpose(emailPurpose)
//                    .isHtml(true)
//                    .build();
//
//            // Create email log
//            EmailLog emailLog = createEmailLog(sendRequest, configuration, template);
//            emailLog.setTemplateVariables(convertMapToJson(variables));
//
//            // Send email
//            boolean sent = sendEmailWithConfiguration(configuration, sendRequest, emailLog);
//
//            // Update status
//            if (sent) {
//                emailLog.setStatus(EmailStatus.SENT);
//                emailLog.setSentAt(LocalDateTime.now());
//                log.info("Templated email sent successfully to: {}", to);
//            } else {
//                emailLog.setStatus(EmailStatus.FAILED);
//                emailLog.setErrorMessage("Failed to send templated email");
//            }
//
//            EmailLog savedLog = emailLogRepository.save(emailLog);
//            return emailMapper.toResponse(savedLog);
//
//        } catch (Exception e) {
//            log.error("Error sending templated email to: {}", to, e);
//            throw new RuntimeException("Failed to send templated email: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    @Transactional
//    public List<EmailLogResponse> sendBulkEmails(List<String> recipients, String templateKey,
//                                                 Map<String, Object> variables, String purpose) {
//        log.info("Sending bulk emails to {} recipients with template: {} and purpose: {}",
//                recipients.size(), templateKey, purpose);
//
//        return recipients.stream()
//                .map(recipient -> sendTemplatedEmail(recipient, templateKey, variables, purpose))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public void retryFailedEmails() {
//        log.info("Starting retry process for failed emails");
//
//        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
//        List<EmailLog> failedEmails = emailLogRepository.findEmailsForRetry(cutoffTime);
//
//        log.info("Found {} failed emails to retry", failedEmails.size());
//
//        for (EmailLog emailLog : failedEmails) {
//            try {
//                retrySingleEmail(emailLog);
//            } catch (Exception e) {
//                log.error("Failed to retry email with ID: {}", emailLog.getId(), e);
//                emailLogRepository.incrementRetryCount(emailLog.getId());
//            }
//        }
//
//        log.info("Completed retry process for failed emails");
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<EmailLogResponse> getEmailLogsByStatus(String status) {
//        log.debug("Fetching email logs by status: {}", status);
//
//        if (status == null) {
//            List<EmailLog> emailLogs = emailLogRepository.findAll();
//            return emailMapper.toLogResponseList(emailLogs);
//        }
//
//        EmailStatus emailStatus = EmailStatus.valueOf(status.toUpperCase());
//        List<EmailLog> emailLogs = emailLogRepository.findByStatus(emailStatus);
//        return emailMapper.toLogResponseList(emailLogs);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<EmailLogResponse> getEmailLogsByPurpose(String purpose) {
//        log.debug("Fetching email logs by purpose: {}", purpose);
//
//        EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
//        List<EmailLog> emailLogs = emailLogRepository.findByPurpose(emailPurpose);
//        return emailMapper.toLogResponseList(emailLogs);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public EmailLogResponse getEmailLogById(Long id) {
//        log.debug("Fetching email log by ID: {}", id);
//
//        EmailLog emailLog = emailLogRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "id", id));
//
//        return emailMapper.toResponse(emailLog);
//    }
//
//    // ========== PRIVATE HELPER METHODS ==========
//
//    private EmailConfiguration getEmailConfiguration(SendEmailRequest request) {
//        if (request.getConfigurationId() != null) {
//            return emailConfigurationRepository.findById(request.getConfigurationId())
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", request.getConfigurationId()));
//        } else {
//            return emailConfigurationRepository.findByPurposeAndIsActiveTrue(request.getPurpose())
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", request.getPurpose().toString()));
//        }
//    }
//
//    private EmailLog createEmailLog(SendEmailRequest request, EmailConfiguration configuration, EmailTemplate template) {
//        String trackingToken = UUID.randomUUID().toString();
//
//        EmailLog emailLog = EmailLog.builder()
//                .emailConfiguration(configuration)
//                .emailTemplate(template)
//                .purpose(request.getPurpose())
//                .recipientEmail(request.getTo())
//                .recipientName(request.getToName())
//                .subject(request.getSubject())
//                .content(request.getContent())
//                .status(EmailStatus.PENDING)
//                .messageId(generateMessageId())
//                .retryCount(0)
//                .trackingToken(trackingToken)
//                .isTracked(true)
//                .build();
//
//        return emailLogRepository.save(emailLog);
//    }
//
//    private boolean sendEmailWithConfiguration(EmailConfiguration configuration, SendEmailRequest request, EmailLog emailLog) {
//        try {
//            JavaMailSender mailSender = createMailSender(configuration);
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            // Set basic email properties
//            helper.setTo(request.getTo());
//            helper.setSubject(request.getSubject());
//            helper.setFrom(configuration.getFromEmail(), configuration.getFromName());
//
//            // Set content type
//            if (request.getIsHtml() != null && request.getIsHtml()) {
//                helper.setText(request.getContent(), true);
//            } else {
//                helper.setText(request.getContent(), false);
//            }
//
//            // Set optional fields
//            if (request.getReplyTo() != null) {
//                helper.setReplyTo(request.getReplyTo());
//            }
//            if (request.getCc() != null) {
//                helper.setCc(request.getCc());
//            }
//            if (request.getBcc() != null) {
//                helper.setBcc(request.getBcc());
//            }
//
//            // Send email
//            mailSender.send(message);
//
//            // Update email log with successful send
//            emailLog.setMessageId(message.getMessageID());
//            return true;
//
//        } catch (Exception e) {
//            log.error("Error sending email with configuration ID: {}", configuration.getId(), e);
//            emailLog.setErrorMessage(e.getMessage());
//            return false;
//        }
//    }
//
//    private JavaMailSender createMailSender(EmailConfiguration configuration) {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//
//        // Basic SMTP configuration
//        mailSender.setHost(configuration.getHost());
//        mailSender.setPort(configuration.getPort());
//        mailSender.setUsername(configuration.getUsername());
//        mailSender.setPassword(configuration.getPassword());
//
//        // JavaMail properties
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", String.valueOf(configuration.getUseTls()));
//        props.put("mail.smtp.ssl.enable", String.valueOf(configuration.getUseSsl()));
//
//        // Timeout settings
//        props.put("mail.smtp.timeout", configuration.getTimeoutMs());
//        props.put("mail.smtp.connectiontimeout", configuration.getTimeoutMs());
//        props.put("mail.smtp.writetimeout", configuration.getTimeoutMs());
//
//        // Debug (only in development)
//        props.put("mail.debug", "false");
//
//        return mailSender;
//    }
//
//    private String processTemplate(String template, Map<String, Object> variables) {
//        if (template == null || variables == null) {
//            return template;
//        }
//
//        String processed = template;
//        for (Map.Entry<String, Object> entry : variables.entrySet()) {
//            String placeholder = "{{" + entry.getKey() + "}}";
//            String value = entry.getValue() != null ? entry.getValue().toString() : "";
//            processed = processed.replace(placeholder, value);
//        }
//
//        return processed;
//    }
//
//    private void retrySingleEmail(EmailLog emailLog) {
//        log.debug("Retrying email with ID: {}", emailLog.getId());
//
//        try {
//            SendEmailRequest retryRequest = SendEmailRequest.builder()
//                    .to(emailLog.getRecipientEmail())
//                    .toName(emailLog.getRecipientName())
//                    .subject(emailLog.getSubject())
//                    .content(emailLog.getContent())
//                    .purpose(emailLog.getPurpose())
//                    .isHtml(true)
//                    .build();
//
//            boolean sent = sendEmailWithConfiguration(emailLog.getEmailConfiguration(), retryRequest, emailLog);
//
//            if (sent) {
//                emailLog.setStatus(EmailStatus.SENT);
//                emailLog.setSentAt(LocalDateTime.now());
//                emailLog.setErrorMessage(null);
//                log.info("Successfully retried email with ID: {}", emailLog.getId());
//            } else {
//                emailLogRepository.incrementRetryCount(emailLog.getId());
//                log.warn("Failed to retry email with ID: {}, retry count: {}",
//                        emailLog.getId(), emailLog.getRetryCount() + 1);
//            }
//
//            emailLogRepository.save(emailLog);
//
//        } catch (Exception e) {
//            emailLogRepository.incrementRetryCount(emailLog.getId());
//            log.error("Error retrying email with ID: {}", emailLog.getId(), e);
//            throw e;
//        }
//    }
//
//    private String generateMessageId() {
//        return "<" + UUID.randomUUID().toString() + "@nexacommerce.com>";
//    }
//
//    private String convertMapToJson(Map<String, Object> map) {
//        if (map == null || map.isEmpty()) {
//            return "{}";
//        }
//
//        try {
//            // Simple JSON conversion - you can use Jackson/Gson for more complex scenarios
//            StringBuilder json = new StringBuilder("{");
//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                json.append("\"").append(entry.getKey()).append("\":\"")
//                        .append(entry.getValue() != null ? entry.getValue().toString() : "")
//                        .append("\",");
//            }
//            if (json.length() > 1) {
//                json.setLength(json.length() - 1); // Remove trailing comma
//            }
//            json.append("}");
//            return json.toString();
//        } catch (Exception e) {
//            log.warn("Failed to convert map to JSON, returning empty object");
//            return "{}";
//        }
//    }
//
//    @Override
//    @Transactional
//    public EmailLogResponse sendEmailWithAttachment(SendEmailRequest request, String attachmentName,
//                                                    byte[] attachmentContent, String contentType) {
//        log.info("Sending email with attachment to: {}, attachment: {}", request.getTo(), attachmentName);
//
//        try {
//            // Get or create email configuration
//            EmailConfiguration configuration = getEmailConfiguration(request);
//
//            // Create email log
//            EmailLog emailLog = createEmailLog(request, configuration, null);
//
//            // Send email with attachment
//            boolean sent = sendEmailWithAttachment(configuration, request, emailLog, attachmentName, attachmentContent, contentType);
//
//            // Update email log status
//            if (sent) {
//                emailLog.setStatus(EmailStatus.SENT);
//                emailLog.setSentAt(LocalDateTime.now());
//                log.info("Email with attachment sent successfully to: {}", request.getTo());
//            } else {
//                emailLog.setStatus(EmailStatus.FAILED);
//                emailLog.setErrorMessage("Failed to send email with attachment");
//            }
//
//            EmailLog savedLog = emailLogRepository.save(emailLog);
//            return emailMapper.toResponse(savedLog);
//
//        } catch (Exception e) {
//            log.error("Error sending email with attachment to: {}", request.getTo(), e);
//            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    @Transactional
//    public EmailLogResponse sendTemplatedEmailWithAttachment(String to, String templateKey,
//                                                             Map<String, Object> variables, String purpose,
//                                                             String attachmentName, byte[] attachmentContent,
//                                                             String contentType) {
//        log.info("Sending templated email with attachment to: {} with template: {}", to, templateKey);
//
//        try {
//            // Get template
//            EmailTemplate template = emailTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey)
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));
//
//            // Get configuration
//            EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
//            EmailConfiguration configuration = emailConfigurationRepository
//                    .findByPurposeAndIsActiveTrue(emailPurpose)
//                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", purpose));
//
//            // Process template
//            String subject = processTemplate(template.getSubject(), variables);
//            String htmlContent = processTemplate(template.getHtmlContent(), variables);
//            String textContent = template.getTextContent() != null ?
//                    processTemplate(template.getTextContent(), variables) : null;
//
//            // Create send request
//            SendEmailRequest sendRequest = SendEmailRequest.builder()
//                    .to(to)
//                    .subject(subject)
//                    .content(htmlContent)
//                    .purpose(emailPurpose)
//                    .isHtml(true)
//                    .build();
//
//            // Create email log
//            EmailLog emailLog = createEmailLog(sendRequest, configuration, template);
//            emailLog.setTemplateVariables(convertMapToJson(variables));
//
//            // Send email with attachment
//            boolean sent = sendEmailWithAttachment(configuration, sendRequest, emailLog, attachmentName, attachmentContent, contentType);
//
//            // Update status
//            if (sent) {
//                emailLog.setStatus(EmailStatus.SENT);
//                emailLog.setSentAt(LocalDateTime.now());
//                log.info("Templated email with attachment sent successfully to: {}", to);
//            } else {
//                emailLog.setStatus(EmailStatus.FAILED);
//                emailLog.setErrorMessage("Failed to send templated email with attachment");
//            }
//
//            EmailLog savedLog = emailLogRepository.save(emailLog);
//            return emailMapper.toResponse(savedLog);
//
//        } catch (Exception e) {
//            log.error("Error sending templated email with attachment to: {}", to, e);
//            throw new RuntimeException("Failed to send templated email with attachment: " + e.getMessage(), e);
//        }
//    }
//
//    private boolean sendEmailWithAttachment(EmailConfiguration configuration, SendEmailRequest request,
//                                            EmailLog emailLog, String attachmentName,
//                                            byte[] attachmentContent, String contentType) {
//        try {
//            JavaMailSender mailSender = createMailSender(configuration);
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            // Set basic email properties
//            helper.setTo(request.getTo());
//            helper.setSubject(request.getSubject());
//            helper.setFrom(configuration.getFromEmail(), configuration.getFromName());
//
//            // Set content type
//            if (request.getIsHtml() != null && request.getIsHtml()) {
//                helper.setText(request.getContent(), true);
//            } else {
//                helper.setText(request.getContent(), false);
//            }
//
//            // Add attachment
//            ByteArrayResource attachmentResource = new ByteArrayResource(attachmentContent);
//            helper.addAttachment(attachmentName, attachmentResource, contentType);
//
//            // Set optional fields
//            if (request.getReplyTo() != null) {
//                helper.setReplyTo(request.getReplyTo());
//            }
//            if (request.getCc() != null) {
//                helper.setCc(request.getCc());
//            }
//            if (request.getBcc() != null) {
//                helper.setBcc(request.getBcc());
//            }
//
//            // Send email
//            mailSender.send(message);
//
//            // Create email attachment record
//            createEmailAttachmentRecord(emailLog, attachmentName, attachmentContent.length, contentType);
//
//            // Update email log with successful send
//            emailLog.setMessageId(message.getMessageID());
//            return true;
//
//        } catch (Exception e) {
//            log.error("Error sending email with attachment using configuration ID: {}", configuration.getId(), e);
//            emailLog.setErrorMessage(e.getMessage());
//            return false;
//        }
//    }
//
//    private void createEmailAttachmentRecord(EmailLog emailLog, String fileName, long fileSize, String contentType) {
//        try {
//            EmailAttachment attachment = EmailAttachment.builder()
//                    .emailLog(emailLog)
//                    .fileName(fileName)
//                    .fileType(contentType)
//                    .fileSize(fileSize)
//                    .filePath("email-attachment/" + emailLog.getId() + "/" + fileName) // Virtual path
//                    .isInline(false)
//                    .build();
//
//            emailAttachmentRepository.save(attachment);
//            log.debug("Created email attachment record for email log ID: {}", emailLog.getId());
//
//        } catch (Exception e) {
//            log.warn("Failed to create email attachment record for email log ID: {}", emailLog.getId(), e);
//            // Don't throw exception - attachment logging failure shouldn't fail email sending
//        }
//    }
//}

package com.roze.nexacommerce.email.service.impl;

import com.roze.nexacommerce.email.dto.request.SendEmailRequest;
import com.roze.nexacommerce.email.dto.response.EmailLogResponse;
import com.roze.nexacommerce.email.entity.EmailAttachment;
import com.roze.nexacommerce.email.entity.EmailConfiguration;
import com.roze.nexacommerce.email.entity.EmailLog;
import com.roze.nexacommerce.email.entity.EmailTemplate;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.enums.EmailStatus;
import com.roze.nexacommerce.email.mapper.EmailMapper;
import com.roze.nexacommerce.email.repository.EmailAttachmentRepository;
import com.roze.nexacommerce.email.repository.EmailConfigurationRepository;
import com.roze.nexacommerce.email.repository.EmailLogRepository;
import com.roze.nexacommerce.email.repository.EmailTemplateRepository;
import com.roze.nexacommerce.email.service.EmailService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailConfigurationRepository emailConfigurationRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailMapper emailMapper;
    private final EmailAttachmentRepository emailAttachmentRepository;

    @Override
    @Transactional
    public EmailLogResponse sendEmail(SendEmailRequest request) {
        log.info("Sending email to: {} with purpose: {}", request.getTo(), request.getPurpose());

        try {
            // Get or create email configuration
            EmailConfiguration configuration = getEmailConfiguration(request);

            // Create email log
            EmailLog emailLog = createEmailLog(request, configuration, null);

            // Send email
            boolean sent = sendEmailWithConfiguration(configuration, request, emailLog);

            // Update email log status
            if (sent) {
                emailLog.setStatus(EmailStatus.SENT);
                emailLog.setSentAt(LocalDateTime.now());
                log.info("Email sent successfully to: {}, Message ID: {}", request.getTo(), emailLog.getMessageId());
            } else {
                emailLog.setStatus(EmailStatus.FAILED);
                emailLog.setErrorMessage("Failed to send email");
                log.error("Failed to send email to: {}", request.getTo());
            }

            EmailLog savedLog = emailLogRepository.save(emailLog);
            return emailMapper.toResponse(savedLog);

        } catch (Exception e) {
            log.error("Error sending email to: {}", request.getTo(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EmailLogResponse sendTemplatedEmail(String to, String templateKey, Map<String, Object> variables, String purpose) {
        log.info("Sending templated email to: {} with template: {} and purpose: {}", to, templateKey, purpose);

        try {
            // Get template
            EmailTemplate template = emailTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey)
                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));

            // Get configuration
            EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
            EmailConfiguration configuration = emailConfigurationRepository
                    .findByPurposeAndIsActiveTrue(emailPurpose)
                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", purpose));

            // Process template
            String subject = processTemplate(template.getSubject(), variables);
            String htmlContent = processTemplate(template.getHtmlContent(), variables);
            String textContent = template.getTextContent() != null ?
                    processTemplate(template.getTextContent(), variables) : null;

            // Create send request
            SendEmailRequest sendRequest = SendEmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .content(htmlContent)
                    .purpose(emailPurpose)
                    .isHtml(true)
                    .build();

            // Create email log
            EmailLog emailLog = createEmailLog(sendRequest, configuration, template);
            // FIX: Directly assign the Map, no conversion needed
            emailLog.setTemplateVariables(variables != null ? new HashMap<>(variables) : new HashMap<>());

            // Send email
            boolean sent = sendEmailWithConfiguration(configuration, sendRequest, emailLog);

            // Update status
            if (sent) {
                emailLog.setStatus(EmailStatus.SENT);
                emailLog.setSentAt(LocalDateTime.now());
                log.info("Templated email sent successfully to: {}", to);
            } else {
                emailLog.setStatus(EmailStatus.FAILED);
                emailLog.setErrorMessage("Failed to send templated email");
            }

            EmailLog savedLog = emailLogRepository.save(emailLog);
            return emailMapper.toResponse(savedLog);

        } catch (Exception e) {
            log.error("Error sending templated email to: {}", to, e);
            throw new RuntimeException("Failed to send templated email: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<EmailLogResponse> sendBulkEmails(List<String> recipients, String templateKey,
                                                 Map<String, Object> variables, String purpose) {
        log.info("Sending bulk emails to {} recipients with template: {} and purpose: {}",
                recipients.size(), templateKey, purpose);

        return recipients.stream()
                .map(recipient -> sendTemplatedEmail(recipient, templateKey, variables, purpose))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void retryFailedEmails() {
        log.info("Starting retry process for failed emails");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<EmailLog> failedEmails = emailLogRepository.findEmailsForRetry(cutoffTime);

        log.info("Found {} failed emails to retry", failedEmails.size());

        for (EmailLog emailLog : failedEmails) {
            try {
                retrySingleEmail(emailLog);
            } catch (Exception e) {
                log.error("Failed to retry email with ID: {}", emailLog.getId(), e);
                emailLogRepository.incrementRetryCount(emailLog.getId());
            }
        }

        log.info("Completed retry process for failed emails");
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLogResponse> getEmailLogsByStatus(String status) {
        log.debug("Fetching email logs by status: {}", status);

        if (status == null) {
            List<EmailLog> emailLogs = emailLogRepository.findAll();
            return emailMapper.toLogResponseList(emailLogs);
        }

        EmailStatus emailStatus = EmailStatus.valueOf(status.toUpperCase());
        List<EmailLog> emailLogs = emailLogRepository.findByStatus(emailStatus);
        return emailMapper.toLogResponseList(emailLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLogResponse> getEmailLogsByPurpose(String purpose) {
        log.debug("Fetching email logs by purpose: {}", purpose);

        EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
        List<EmailLog> emailLogs = emailLogRepository.findByPurpose(emailPurpose);
        return emailMapper.toLogResponseList(emailLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLogResponse getEmailLogById(Long id) {
        log.debug("Fetching email log by ID: {}", id);

        EmailLog emailLog = emailLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "id", id));

        return emailMapper.toResponse(emailLog);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private EmailConfiguration getEmailConfiguration(SendEmailRequest request) {
        if (request.getConfigurationId() != null) {
            return emailConfigurationRepository.findById(request.getConfigurationId())
                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "id", request.getConfigurationId()));
        } else {
            return emailConfigurationRepository.findByPurposeAndIsActiveTrue(request.getPurpose())
                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", request.getPurpose().toString()));
        }
    }

    private EmailLog createEmailLog(SendEmailRequest request, EmailConfiguration configuration, EmailTemplate template) {
        String trackingToken = UUID.randomUUID().toString();

        EmailLog emailLog = EmailLog.builder()
                .emailConfiguration(configuration)
                .emailTemplate(template)
                .purpose(request.getPurpose())
                .recipientEmail(request.getTo())
                .recipientName(request.getToName())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(EmailStatus.PENDING)
                .messageId(generateMessageId())
                .retryCount(0)
                .trackingToken(trackingToken)
                .isTracked(true)
                .templateVariables(new HashMap<>()) // Initialize with empty map
                .build();

        return emailLogRepository.save(emailLog);
    }

    private boolean sendEmailWithConfiguration(EmailConfiguration configuration, SendEmailRequest request, EmailLog emailLog) {
        try {
            JavaMailSender mailSender = createMailSender(configuration);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set basic email properties
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setFrom(configuration.getFromEmail(), configuration.getFromName());

            // Set content type
            if (request.getIsHtml() != null && request.getIsHtml()) {
                helper.setText(request.getContent(), true);
            } else {
                helper.setText(request.getContent(), false);
            }

            // Set optional fields
            if (request.getReplyTo() != null) {
                helper.setReplyTo(request.getReplyTo());
            }
            if (request.getCc() != null) {
                helper.setCc(request.getCc());
            }
            if (request.getBcc() != null) {
                helper.setBcc(request.getBcc());
            }

            // Send email
            mailSender.send(message);

            // Update email log with successful send
            emailLog.setMessageId(message.getMessageID());
            return true;

        } catch (Exception e) {
            log.error("Error sending email with configuration ID: {}", configuration.getId(), e);
            emailLog.setErrorMessage(e.getMessage());
            return false;
        }
    }

    private JavaMailSender createMailSender(EmailConfiguration configuration) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Basic SMTP configuration
        mailSender.setHost(configuration.getHost());
        mailSender.setPort(configuration.getPort());
        mailSender.setUsername(configuration.getUsername());
        mailSender.setPassword(configuration.getPassword());

        // JavaMail properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(configuration.getUseTls()));
        props.put("mail.smtp.ssl.enable", String.valueOf(configuration.getUseSsl()));

        // Timeout settings
        props.put("mail.smtp.timeout", configuration.getTimeoutMs());
        props.put("mail.smtp.connectiontimeout", configuration.getTimeoutMs());
        props.put("mail.smtp.writetimeout", configuration.getTimeoutMs());

        // Debug (only in development)
        props.put("mail.debug", "false");

        return mailSender;
    }

    private String processTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String processed = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }

        return processed;
    }

    private void retrySingleEmail(EmailLog emailLog) {
        log.debug("Retrying email with ID: {}", emailLog.getId());

        try {
            SendEmailRequest retryRequest = SendEmailRequest.builder()
                    .to(emailLog.getRecipientEmail())
                    .toName(emailLog.getRecipientName())
                    .subject(emailLog.getSubject())
                    .content(emailLog.getContent())
                    .purpose(emailLog.getPurpose())
                    .isHtml(true)
                    .build();

            boolean sent = sendEmailWithConfiguration(emailLog.getEmailConfiguration(), retryRequest, emailLog);

            if (sent) {
                emailLog.setStatus(EmailStatus.SENT);
                emailLog.setSentAt(LocalDateTime.now());
                emailLog.setErrorMessage(null);
                log.info("Successfully retried email with ID: {}", emailLog.getId());
            } else {
                emailLogRepository.incrementRetryCount(emailLog.getId());
                log.warn("Failed to retry email with ID: {}, retry count: {}",
                        emailLog.getId(), emailLog.getRetryCount() + 1);
            }

            emailLogRepository.save(emailLog);

        } catch (Exception e) {
            emailLogRepository.incrementRetryCount(emailLog.getId());
            log.error("Error retrying email with ID: {}", emailLog.getId(), e);
            throw e;
        }
    }

    private String generateMessageId() {
        return "<" + UUID.randomUUID().toString() + "@nexacommerce.com>";
    }

    // REMOVE THIS METHOD - No longer needed
    // private String convertMapToJson(Map<String, Object> map) { ... }

    @Override
    @Transactional
    public EmailLogResponse sendEmailWithAttachment(SendEmailRequest request, String attachmentName,
                                                    byte[] attachmentContent, String contentType) {
        log.info("Sending email with attachment to: {}, attachment: {}", request.getTo(), attachmentName);

        try {
            // Get or create email configuration
            EmailConfiguration configuration = getEmailConfiguration(request);

            // Create email log
            EmailLog emailLog = createEmailLog(request, configuration, null);

            // Send email with attachment
            boolean sent = sendEmailWithAttachment(configuration, request, emailLog, attachmentName, attachmentContent, contentType);

            // Update email log status
            if (sent) {
                emailLog.setStatus(EmailStatus.SENT);
                emailLog.setSentAt(LocalDateTime.now());
                log.info("Email with attachment sent successfully to: {}", request.getTo());
            } else {
                emailLog.setStatus(EmailStatus.FAILED);
                emailLog.setErrorMessage("Failed to send email with attachment");
            }

            EmailLog savedLog = emailLogRepository.save(emailLog);
            return emailMapper.toResponse(savedLog);

        } catch (Exception e) {
            log.error("Error sending email with attachment to: {}", request.getTo(), e);
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EmailLogResponse sendTemplatedEmailWithAttachment(String to, String templateKey,
                                                             Map<String, Object> variables, String purpose,
                                                             String attachmentName, byte[] attachmentContent,
                                                             String contentType) {
        log.info("Sending templated email with attachment to: {} with template: {}", to, templateKey);

        try {
            // Get template
            EmailTemplate template = emailTemplateRepository.findByTemplateKeyAndIsActiveTrue(templateKey)
                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "templateKey", templateKey));

            // Get configuration
            EmailPurpose emailPurpose = EmailPurpose.valueOf(purpose.toUpperCase());
            EmailConfiguration configuration = emailConfigurationRepository
                    .findByPurposeAndIsActiveTrue(emailPurpose)
                    .orElseThrow(() -> new ResourceNotFoundException("EmailConfiguration", "purpose", purpose));

            // Process template
            String subject = processTemplate(template.getSubject(), variables);
            String htmlContent = processTemplate(template.getHtmlContent(), variables);
            String textContent = template.getTextContent() != null ?
                    processTemplate(template.getTextContent(), variables) : null;

            // Create send request
            SendEmailRequest sendRequest = SendEmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .content(htmlContent)
                    .purpose(emailPurpose)
                    .isHtml(true)
                    .build();

            // Create email log
            EmailLog emailLog = createEmailLog(sendRequest, configuration, template);
            // FIX: Directly assign the Map, no conversion needed
            emailLog.setTemplateVariables(variables != null ? new HashMap<>(variables) : new HashMap<>());

            // Send email with attachment
            boolean sent = sendEmailWithAttachment(configuration, sendRequest, emailLog, attachmentName, attachmentContent, contentType);

            // Update status
            if (sent) {
                emailLog.setStatus(EmailStatus.SENT);
                emailLog.setSentAt(LocalDateTime.now());
                log.info("Templated email with attachment sent successfully to: {}", to);
            } else {
                emailLog.setStatus(EmailStatus.FAILED);
                emailLog.setErrorMessage("Failed to send templated email with attachment");
            }

            EmailLog savedLog = emailLogRepository.save(emailLog);
            return emailMapper.toResponse(savedLog);

        } catch (Exception e) {
            log.error("Error sending templated email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send templated email with attachment: " + e.getMessage(), e);
        }
    }

    private boolean sendEmailWithAttachment(EmailConfiguration configuration, SendEmailRequest request,
                                            EmailLog emailLog, String attachmentName,
                                            byte[] attachmentContent, String contentType) {
        try {
            JavaMailSender mailSender = createMailSender(configuration);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set basic email properties
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setFrom(configuration.getFromEmail(), configuration.getFromName());

            // Set content type
            if (request.getIsHtml() != null && request.getIsHtml()) {
                helper.setText(request.getContent(), true);
            } else {
                helper.setText(request.getContent(), false);
            }

            // Add attachment
            ByteArrayResource attachmentResource = new ByteArrayResource(attachmentContent);
            helper.addAttachment(attachmentName, attachmentResource, contentType);

            // Set optional fields
            if (request.getReplyTo() != null) {
                helper.setReplyTo(request.getReplyTo());
            }
            if (request.getCc() != null) {
                helper.setCc(request.getCc());
            }
            if (request.getBcc() != null) {
                helper.setBcc(request.getBcc());
            }

            // Send email
            mailSender.send(message);

            // Create email attachment record
            createEmailAttachmentRecord(emailLog, attachmentName, attachmentContent.length, contentType);

            // Update email log with successful send
            emailLog.setMessageId(message.getMessageID());
            return true;

        } catch (Exception e) {
            log.error("Error sending email with attachment using configuration ID: {}", configuration.getId(), e);
            emailLog.setErrorMessage(e.getMessage());
            return false;
        }
    }

    private void createEmailAttachmentRecord(EmailLog emailLog, String fileName, long fileSize, String contentType) {
        try {
            EmailAttachment attachment = EmailAttachment.builder()
                    .emailLog(emailLog)
                    .fileName(fileName)
                    .fileType(contentType)
                    .fileSize(fileSize)
                    .filePath("email-attachment/" + emailLog.getId() + "/" + fileName) // Virtual path
                    .isInline(false)
                    .build();

            emailAttachmentRepository.save(attachment);
            log.debug("Created email attachment record for email log ID: {}", emailLog.getId());

        } catch (Exception e) {
            log.warn("Failed to create email attachment record for email log ID: {}", emailLog.getId(), e);
            // Don't throw exception - attachment logging failure shouldn't fail email sending
        }
    }
}