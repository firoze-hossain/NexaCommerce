package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_purpose_mappings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "purpose"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePurposeMapping extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmailTemplate emailTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailPurpose purpose;
    
    @Builder.Default
    private Boolean isDefault = false;
    
    @Builder.Default
    private Integer priority = 1;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplatePurposeMapping)) return false;
        TemplatePurposeMapping that = (TemplatePurposeMapping) o;
        return getId() != null && getId().equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "TemplatePurposeMapping{" +
                "id=" + getId() +
                ", purpose=" + purpose +
                ", isDefault=" + isDefault +
                '}';
    }
}