package com.roze.nexacommerce.email.entity;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTracking extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_log_id", nullable = false)
    private EmailLog emailLog;
    
    @Column(nullable = false)
    private String trackingToken;
    
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    
    private String ipAddress;
    private String userAgent;
    private String country;
    private String city;
    
    private String clickUrl;
    private Integer clickCount;
    
    private String deviceType; // MOBILE, DESKTOP, TABLET
    private String browser;
    private String platform;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailTracking)) return false;
        EmailTracking that = (EmailTracking) o;
        return getId() != null && getId().equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}