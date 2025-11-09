package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import com.roze.nexacommerce.email.enums.EmailStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "email_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private EmailConfiguration emailConfiguration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private EmailTemplate emailTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailPurpose purpose;
    
    @Column(nullable = false)
    private String recipientEmail;
    
    private String recipientName;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;
    
    private String messageId; // Provider's message ID
    
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String providerResponse;
    
    private Integer retryCount;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> templateVariables; // Chan
    
    private String ipAddress;
    private String userAgent;
    
    // Tracking
    private String trackingToken;
    private Boolean isTracked;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailLog)) return false;
        EmailLog emailLog = (EmailLog) o;
        return getId() != null && getId().equals(emailLog.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "EmailLog{" +
                "id=" + getId() +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", purpose=" + purpose +
                ", status=" + status +
                '}';
    }
}