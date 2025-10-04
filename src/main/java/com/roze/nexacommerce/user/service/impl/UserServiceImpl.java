package com.roze.nexacommerce.user.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.dto.request.UserRequest;
import com.roze.nexacommerce.user.dto.request.UserUpdateRequest;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.mapper.UserMapper;
import com.roze.nexacommerce.user.repository.RoleRepository;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        UserResponse response = userMapper.toResponse(savedUser);
        log.info("User created Successfully:{}", response);
        return response;
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();
        log.info("Users loaded successfully: {} ", userResponses);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.<UserResponse>builder()
                .items(userResponses)
                .totalItems(userPage.getTotalElements())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .build();
        log.info("Paginated users loaded successfully: {} ", paginatedResponse);
        return paginatedResponse;
    }

    @Override
    public PaginatedResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        Page<User> userPage = userRepository.findByRoleName(roleName, pageable);
        List<UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return PaginatedResponse.<UserResponse>builder()
                .items(userResponses)
                .totalItems(userPage.getTotalElements())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateResourceException("User with email " + userRequest.getEmail() + " already exists");
        }

        Role role = roleRepository.findById(userRequest.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", userRequest.getRoleId()));

        userMapper.updateEntity(userRequest, user);
        user.setRole(role);

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        UserResponse response = userMapper.toResponse(updatedUser);
        log.info("User updated successfully: {}"+response);
        return response;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(true);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }
}
