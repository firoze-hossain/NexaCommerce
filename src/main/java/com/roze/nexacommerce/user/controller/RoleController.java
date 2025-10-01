package com.roze.nexacommerce.user.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.RoleRequest;
import com.roze.nexacommerce.user.dto.request.UpdateRolePermissionsRequest;
import com.roze.nexacommerce.user.dto.response.RoleResponse;
import com.roze.nexacommerce.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/roles")
@PreAuthorize("hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class RoleController extends BaseController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<RoleResponse>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        PaginatedResponse<RoleResponse> roles = roleService.getAllRoles(pageable);
        return paginated(roles, "Roles retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ok(response, "Role retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<BaseResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return created(response, "Role created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ok(response, "Role updated successfully");
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<BaseResponse<RoleResponse>> updateRolePermissions(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRolePermissionsRequest request) {
        RoleResponse response = roleService.updateRolePermissions(id, request.getPermissionIds());
        return ok(response, "Role permissions updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return noContent("Role deleted successfully");
    }
}