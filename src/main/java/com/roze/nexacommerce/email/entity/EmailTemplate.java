package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.email.enums.EmailCategory;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Type;
@Entity
@Table(name = "email_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String templateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailCategory category;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String preheader;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String htmlContent;

    @Column(columnDefinition = "LONGTEXT")
    private String textContent;

    private String language;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isSystemTemplate = false;

    @Column(name = "template_version")
    @Builder.Default
    private Integer templateVersion = 1;

//    // Supported purposes for this template
//    //@Type(JsonType.class)
//    @Column(columnDefinition = "jsonb")
//    private String supportedPurposes;
//
//    // Available template variables
//    //@Type(JsonType.class)
//    @Column(columnDefinition = "jsonb")
//    private String availableVariables;
//
//    //@Type(JsonType.class)
//    @Column(columnDefinition = "jsonb")
//    private String defaultVariables;
// Supported purposes for this template - Store as JSONB
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private List<String> supportedPurposes;

    // Available template variables - Store as JSONB
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> availableVariables;

    // Default variables - Store as JSONB
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> defaultVariables;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_configuration_id")
    private EmailConfiguration defaultConfiguration;

    @OneToMany(mappedBy = "emailTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmailLog> emailLogs = new ArrayList<>();

    @OneToMany(mappedBy = "emailTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TemplatePurposeMapping> purposeMappings = new ArrayList<>();

    // Helper methods
    public boolean supportsPurpose(EmailPurpose purpose) {
        // Implementation to check if template supports the purpose
        return true; // Simplified - implement JSON parsing logic
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailTemplate)) return false;
        EmailTemplate that = (EmailTemplate) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EmailTemplate{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", templateKey='" + templateKey + '\'' +
                ", category=" + category +
                ", templateVersion=" + templateVersion +
                '}';
    }
}