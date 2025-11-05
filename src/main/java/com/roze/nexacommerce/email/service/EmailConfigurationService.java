package com.roze.nexacommerce.email.service;

import com.roze.nexacommerce.email.dto.request.EmailConfigurationRequest;
import com.roze.nexacommerce.email.dto.response.EmailConfigurationResponse;
import com.roze.nexacommerce.email.dto.response.TestEmailResponse;

import java.util.List;

public interface EmailConfigurationService {
    EmailConfigurationResponse createConfiguration(EmailConfigurationRequest request);

    EmailConfigurationResponse updateConfiguration(Long id, EmailConfigurationRequest request);

    EmailConfigurationResponse getConfigurationById(Long id);

    List<EmailConfigurationResponse> getAllConfigurations();

    List<EmailConfigurationResponse> getActiveConfigurations();

    void deleteConfiguration(Long id);

    EmailConfigurationResponse activateConfiguration(Long id);

    EmailConfigurationResponse setAsDefault(Long id);

    TestEmailResponse testConfiguration(Long id);

    TestEmailResponse testConfigurationWithEmail(Long id, String testEmail);

    EmailConfigurationResponse getConfigurationByPurpose(String purpose);
}