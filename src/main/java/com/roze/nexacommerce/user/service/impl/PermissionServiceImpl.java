package com.roze.nexacommerce.user.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.dto.request.PermissionRequest;
import com.roze.nexacommerce.user.dto.response.PermissionResponse;
import com.roze.nexacommerce.user.entity.Permission;
import com.roze.nexacommerce.user.mapper.PermissionMapper;
import com.roze.nexacommerce.user.repository.PermissionRepository;
import com.roze.nexacommerce.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Permission with name '" + request.getName() + "' already exists");
        }

        Permission permission = permissionMapper.toEntity(request);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponse(savedPermission);
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return permissionMapper.toResponse(permission);
    }

    @Override
    public PaginatedResponse<PermissionResponse> getAllPermissions(Pageable pageable) {
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);
        List<PermissionResponse> permissionResponses = permissionPage.getContent()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();

        return PaginatedResponse.<PermissionResponse>builder()
                .items(permissionResponses)
                .totalItems(permissionPage.getTotalElements())
                .currentPage(permissionPage.getNumber())
                .pageSize(permissionPage.getSize())
                .totalPages(permissionPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if (!permission.getName().equals(request.getName()) &&
                permissionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Permission with name '" + request.getName() + "' already exists");
        }

        permissionMapper.updateEntity(request, permission);
        Permission updatedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponse(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if (!permission.getRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete permission. It is currently assigned to " +
                    permission.getRoles().size() + " role(s).");
        }

        permissionRepository.delete(permission);
    }

    @Override
    public Permission findByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", name));
    }

    @Override
    public Set<Permission> findByNameIn(List<String> permissionNames) {
        return permissionRepository.findByNameIn(permissionNames);
    }

    @Override
    public Set<Permission> findByIds(Set<Long> permissionIds) {
        return permissionRepository.findByIdIn(permissionIds);
    }
}