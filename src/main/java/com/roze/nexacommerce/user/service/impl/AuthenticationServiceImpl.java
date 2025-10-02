package com.roze.nexacommerce.user.service.impl;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new AuthenticationException("User account is disabled");
        } catch (LockedException e) {
            throw new AuthenticationException("User account is locked");
        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return LoginResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration/1000)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            if (isTokenBlacklisted(refreshToken)) {
                throw new AuthenticationException("Refresh token is invalid");
            }
            String username = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!user.getActive()) {
                throw new AuthenticationException("User account is deactivated");
            }
            if (!jwtUtil.isTokenValid(refreshToken, user)) {
                throw new AuthenticationException("Invalid refresh token");
            }
            String newAccessToken = jwtUtil.generateToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            blacklistToken(refreshToken, jwtUtil.extractExpiration(refreshToken));
            log.info("Token refreshed for user:{}", user.getEmail());
            return LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtExpiration / 1000)
                    .user(mapToUserResponse(user))
                    .build();
        } catch (Exception e) {
            log.error("Token refresh failed:{}", e.getMessage());
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Date expirationDate = jwtUtil.extractExpiration(token);
            blacklistToken(token, expirationDate);
            log.info("User logged out successfully.Token blacklisted");
        } catch (Exception e) {
            log.error("Logout failed:{}", e.getMessage());
            throw new AuthenticationException("Logout failed");
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void blacklistToken(String token, Date expirationDate) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expirationDate.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
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
