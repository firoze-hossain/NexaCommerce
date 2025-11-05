package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.email.enums.EmailProvider;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "email_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfiguration extends BaseEntity {

    @Column(nullable = false)
    private String configName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailPurpose purpose;

    @Column(nullable = false)
    private String fromEmail;

    private String fromName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailProvider provider;

    // SMTP Configuration
    private String host;
    private Integer port;
    private String username;

    @Column(length = 500)
    private String password; // Encrypted

    // Advanced Settings
    @Builder.Default
    private Boolean useTls = true;

    @Builder.Default
    private Boolean useSsl = false;

    private String encryptionType;

    // Provider-specific configurations
    private String apiKey; // Encrypted
    private String apiSecret; // Encrypted
    private String domain;
    private String region; // For AWS SES

    // Configuration
    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Integer maxRetries = 3;

    @Builder.Default
    private Integer timeoutMs = 30000;

    // Rate limiting
    @Builder.Default
    private Integer rateLimitPerHour = 1000;

    // Templates
    private String templateId;
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String templateData;

    @Column(columnDefinition = "TEXT")
    private String customHeaders;

    // Relationships
    @OneToMany(mappedBy = "emailConfiguration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmailLog> emailLogs = new ArrayList<>();

    @OneToMany(mappedBy = "defaultConfiguration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmailTemplate> emailTemplates = new ArrayList<>();

    // Helper methods
    public boolean canSendEmail() {
        return isActive && (maxRetries == null || maxRetries > 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailConfiguration)) return false;
        EmailConfiguration that = (EmailConfiguration) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EmailConfiguration{" +
                "id=" + getId() +
                ", configName='" + configName + '\'' +
                ", purpose=" + purpose +
                ", fromEmail='" + fromEmail + '\'' +
                ", provider=" + provider +
                ", isActive=" + isActive +
                '}';
    }
}