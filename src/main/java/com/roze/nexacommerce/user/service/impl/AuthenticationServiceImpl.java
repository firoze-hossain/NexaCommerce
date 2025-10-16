package com.roze.nexacommerce.user.service.impl;

import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.AuthenticationException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.exception.ValidationException;
import com.roze.nexacommerce.security.JwtUtil;
import com.roze.nexacommerce.user.dto.request.LoginRequest;
import com.roze.nexacommerce.user.dto.response.LoginResponse;
import com.roze.nexacommerce.user.dto.response.RoleResponse;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.user.service.AuthenticationService;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist";
    private final CustomerProfileRepository customerProfileRepository;
    private final VendorProfileRepository vendorProfileRepository;
    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("🔐 Login attempt for email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            log.info("✅ Authentication successful for email: {}", request.getEmail());
        } catch (DisabledException e) {
            log.warn("❌ Login failed - account disabled: {}", request.getEmail());
            throw new AuthenticationException("User account is disabled");
        } catch (LockedException e) {
            log.warn("❌ Login failed - account locked: {}", request.getEmail());
            throw new AuthenticationException("User account is locked");
        } catch (BadCredentialsException e) {
            log.warn("❌ Login failed - bad credentials: {}", request.getEmail());
            throw new ValidationException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("❌ User not found after successful authentication: {}", request.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("✅ Login successful for user: {}, Token generated", user.getEmail());
        // Get customer and vendor profiles
        Optional<CustomerProfile> customerProfile = customerProfileRepository.findByUserEmail(user.getEmail());
        Optional<VendorProfile> vendorProfile = vendorProfileRepository.findByUserEmail(user.getEmail());
// Build user response with customer and vendor IDs
        UserResponse userResponse = mapToUserResponse(user);
        customerProfile.ifPresent(profile -> userResponse.setCustomerId(profile.getId()));
        vendorProfile.ifPresent(profile -> userResponse.setVendorId(profile.getId()));
        // Build user response with customer and vendor IDs
        return LoginResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000)
                .user(userResponse)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("🔄 Token refresh attempt");
        log.debug("📋 Refresh token length: {}", refreshToken != null ? refreshToken.length() : 0);

        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.error("❌ Refresh token is null or empty");
                throw new AuthenticationException("Refresh token is required");
            }

            if (isTokenBlacklisted(refreshToken)) {
                log.warn("❌ Refresh token is blacklisted");
                throw new AuthenticationException("Refresh token is invalid");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            log.debug("🔍 Extracted username from refresh token: {}", username);

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.error("❌ User not found during token refresh: {}", username);
                        return new ResourceNotFoundException("User not found");
                    });

            if (!user.getActive()) {
                log.warn("❌ User account is deactivated: {}", username);
                throw new AuthenticationException("User account is deactivated");
            }

            if (!jwtUtil.isTokenValid(refreshToken, user)) {
                log.warn("❌ Refresh token validation failed for user: {}", username);
                throw new AuthenticationException("Invalid refresh token");
            }

            String newAccessToken = jwtUtil.generateToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            // Blacklist the old refresh token
            blacklistToken(refreshToken, jwtUtil.extractExpiration(refreshToken));

            log.info("✅ Token refreshed successfully for user: {}", user.getEmail());
            log.debug("📋 New tokens generated - Access: {}, Refresh: {}",
                    newAccessToken.length(), newRefreshToken.length());

            return LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtExpiration / 1000)
                    .user(mapToUserResponse(user))
                    .build();
        } catch (Exception e) {
            log.error("❌ Token refresh failed: {}", e.getMessage(), e);
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        log.info("🚪 Logout attempt");
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                log.debug("📋 Token length after Bearer removal: {}", token.length());
            }

            if (token == null || token.trim().isEmpty()) {
                log.warn("⚠️ No token provided for logout");
                return;
            }

            Date expirationDate = jwtUtil.extractExpiration(token);
            blacklistToken(token, expirationDate);
            log.info("✅ User logged out successfully. Token blacklisted");
        } catch (Exception e) {
            log.error("❌ Logout failed: {}", e.getMessage(), e);
            throw new AuthenticationException("Logout failed");
        }
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        log.info("👤 Getting current user for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        // Get customer profile if exists
        Optional<CustomerProfile> customerProfile = customerProfileRepository.findByUserEmail(email);
        Optional<VendorProfile> vendorProfile = vendorProfileRepository.findByUserEmail(email);

        UserResponse userResponse = mapToUserResponse(user);

        // Add customer ID if customer profile exists
        customerProfile.ifPresent(profile -> userResponse.setCustomerId(profile.getId()));
        // Add vendor ID if vendor profile exists
        vendorProfile.ifPresent(profile -> userResponse.setVendorId(profile.getId()));

        log.info("✅ Current user retrieved successfully: {}", email);
        return userResponse;
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        log.debug("🔍 Token blacklist check: {}", isBlacklisted);
        return isBlacklisted;
    }

    private void blacklistToken(String token, Date expirationDate) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expirationDate.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            log.debug("📋 Token blacklisted with TTL: {} ms", ttl);
        } else {
            log.warn("⚠️ Token already expired, not blacklisting");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(mapToRoleResponse(user.getRole()))
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}