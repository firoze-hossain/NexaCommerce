package com.roze.nexacommerce.user.mapper;

import com.roze.nexacommerce.user.dto.request.UserRequest;
import com.roze.nexacommerce.user.dto.request.UserUpdateRequest;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import com.roze.nexacommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final ModelMapper modelMapper;
    private final RoleMapper roleMapper;

    public User toEntity(UserRequest request) {
        return modelMapper.map(request, User.class);
    }

    public UserResponse toResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        if (user.getRole() != null) {
            response.setRole(roleMapper.toResponse(user.getRole()));
        }
        return response;
    }

    public void updateEntity(UserUpdateRequest request, User user) {
        modelMapper.map(request, user);
    }
}
