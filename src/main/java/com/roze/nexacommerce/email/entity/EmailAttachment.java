package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_log_id", nullable = false)
    private EmailLog emailLog;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    private String fileType;
    
    private Long fileSize;
    
    private String contentId; // For inline images
    
    @Builder.Default
    private Boolean isInline = false;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAttachment)) return false;
        EmailAttachment that = (EmailAttachment) o;
        return getId() != null && getId().equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}