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
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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
                .user(mapToUserResponse(user))
                .build();
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
