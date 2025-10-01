package com.roze.nexacommerce.user.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.dto.request.RoleRequest;
import com.roze.nexacommerce.user.dto.response.RoleResponse;
import com.roze.nexacommerce.user.entity.Permission;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.mapper.RoleMapper;
import com.roze.nexacommerce.user.repository.PermissionRepository;
import com.roze.nexacommerce.user.repository.RoleRepository;
import com.roze.nexacommerce.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role with name '" + request.getName() + "' already exists");
        }

        Role role = roleMapper.toEntity(request);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponse(savedRole);
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return roleMapper.toResponse(role);
    }

    @Override
    public PaginatedResponse<RoleResponse> getAllRoles(Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(pageable);
        List<RoleResponse> roleResponses = rolePage.getContent()
                .stream()
                .map(roleMapper::toResponse)
                .toList();

        return PaginatedResponse.<RoleResponse>builder()
                .items(roleResponses)
                .totalItems(rolePage.getTotalElements())
                .currentPage(rolePage.getNumber())
                .pageSize(rolePage.getSize())
                .totalPages(rolePage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role with name '" + request.getName() + "' already exists");
        }

        roleMapper.updateEntity(request, role);
        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = permissionRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(Long id, Set<Long> permissionIds) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        Set<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
        role.setPermissions(permissions);

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role. It is currently assigned to " +
                    role.getUsers().size() + " user(s).");
        }

        roleRepository.delete(role);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }
}