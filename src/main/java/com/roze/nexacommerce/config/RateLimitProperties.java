package com.roze.nexacommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.rate-limit")
public class RateLimitProperties {
    private int capacity = 100;
    private int refillTokens = 100;
    private int refillDuration = 1; // minutes
    private boolean enabled = true;
    
    // Per endpoint limits
    private EndpointLimits endpoints = new EndpointLimits();
    
    @Data
    public static class EndpointLimits {
        private int auth = 10; // login attempts per minute
        private int publicApi = 100; // public endpoints
        private int userApi = 500; // user endpoints
        private int adminApi = 1000; // admin endpoints
    }
}