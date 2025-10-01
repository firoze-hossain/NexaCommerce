package com.roze.nexacommerce.user.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.PermissionRequest;
import com.roze.nexacommerce.user.dto.response.PermissionResponse;
import com.roze.nexacommerce.user.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/permissions")
@PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class PermissionController extends BaseController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<PermissionResponse>>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        PaginatedResponse<PermissionResponse> permissions = permissionService.getAllPermissions(pageable);
        return paginated(permissions, "Permissions retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<BaseResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return created(response, "Permission created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ok(response, "Permission updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return noContent("Permission deleted successfully");
    }
}