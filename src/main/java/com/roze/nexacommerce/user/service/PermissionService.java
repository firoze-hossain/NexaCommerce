package com.roze.nexacommerce.user.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.PermissionRequest;
import com.roze.nexacommerce.user.dto.response.PermissionResponse;
import com.roze.nexacommerce.user.entity.Permission;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);

    PermissionResponse getPermissionById(Long id);

    PaginatedResponse<PermissionResponse> getAllPermissions(Pageable pageable);

    PermissionResponse updatePermission(Long id, PermissionRequest request);

    void deletePermission(Long id);

    Permission findByName(String name);

    Set<Permission> findByNameIn(List<String> permissionNames);

    Set<Permission> findByIds(Set<Long> permissionIds);
}