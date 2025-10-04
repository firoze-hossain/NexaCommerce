package com.roze.nexacommerce.user.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.UserRequest;
import com.roze.nexacommerce.user.dto.request.UserUpdateRequest;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(UserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    PaginatedResponse<UserResponse> getAllUsers(Pageable pageable);

    PaginatedResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable);

    UserResponse updateUser(Long id, UserUpdateRequest userRequest);

    void deleteUser(Long id);

    UserResponse deactivateUser(Long id);

    UserResponse activateUser(Long id);
}
