package com.roze.nexacommerce.email.dto.request;

import com.roze.nexacommerce.email.enums.EmailPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    private String to;
    
    private String toName;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String content;
    
    @NotNull(message = "Email purpose is required")
    private EmailPurpose purpose;
    
    private String templateKey;
    private Map<String, Object> templateVariables;
    
    private Long configurationId;
    private Long templateId;
    
    private Boolean isHtml;
    private String replyTo;
    private String cc;
    private String bcc;
}