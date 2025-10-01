package com.roze.nexacommerce.user.mapper;

import com.roze.nexacommerce.user.dto.request.RoleRequest;
import com.roze.nexacommerce.user.dto.response.RoleResponse;
import com.roze.nexacommerce.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {
    private final ModelMapper modelMapper;
    private final PermissionMapper permissionMapper;

    public Role toEntity(RoleRequest request) {
        return modelMapper.map(request, Role.class);
    }

    public RoleResponse toResponse(Role role) {
        RoleResponse response = modelMapper.map(role, RoleResponse.class);

        if (role.getPermissions() != null) {
            response.setPermissions(role.getPermissions().stream()
                    .map(permissionMapper::toResponse)
                    .collect(Collectors.toSet()));
        }
        return response;
    }

    public void updateEntity(RoleRequest request, Role role) {
        modelMapper.map(request, role);
    }
}
