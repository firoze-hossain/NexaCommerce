package com.roze.nexacommerce.email.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEmailRequest {
    @NotBlank(message = "Test email is required")
    private String testEmail;
    
    private Long configurationId;
    private String purpose;
}