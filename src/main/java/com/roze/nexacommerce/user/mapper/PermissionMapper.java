package com.roze.nexacommerce.user.mapper;

import com.roze.nexacommerce.user.dto.request.PermissionRequest;
import com.roze.nexacommerce.user.dto.response.PermissionResponse;
import com.roze.nexacommerce.user.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionMapper {
    private final ModelMapper modelMapper;

    public Permission toEntity(PermissionRequest request) {
        return modelMapper.map(request, Permission.class);
    }

    public PermissionResponse toResponse(Permission permission) {
        return modelMapper.map(permission, PermissionResponse.class);
    }

    public Set<PermissionResponse> toResponseSet(Set<Permission> permissions) {
        return permissions.stream().map(this::toResponse)
                .collect(Collectors.toSet());
    }

    public void updateEntity(PermissionRequest request, Permission permission) {
        modelMapper.map(request, permission);
    }
}
