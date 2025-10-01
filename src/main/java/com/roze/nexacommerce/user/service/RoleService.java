package com.roze.nexacommerce.user.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.RoleRequest;
import com.roze.nexacommerce.user.dto.response.RoleResponse;
import com.roze.nexacommerce.user.entity.Role;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);

    RoleResponse getRoleById(Long id);

    PaginatedResponse<RoleResponse> getAllRoles(Pageable pageable);

    RoleResponse updateRole(Long id, RoleRequest request);

    RoleResponse updateRolePermissions(Long id, Set<Long> permissionIds);

    void deleteRole(Long id);

    Role findByName(String name);
}