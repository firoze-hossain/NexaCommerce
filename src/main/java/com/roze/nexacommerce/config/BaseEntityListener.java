package com.roze.nexacommerce.config;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class BaseEntityListener {
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return sra.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "SYSTEM";
        }

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // In case of multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    @PrePersist
    public void onPrePersist(BaseEntity entity) {
        HttpServletRequest request = getCurrentRequest();
        entity.setIpAddress(getClientIpAddress(request));

        // Ensure version is initialized
        if (entity.getVersion() == null) {
            entity.setVersion(0L);
        }
    }

    @PreUpdate
    public void onPreUpdate(BaseEntity entity) {
        HttpServletRequest request = getCurrentRequest();
        entity.setIpAddress(getClientIpAddress(request));
    }
}
