// ReturnPolicyController.java
package com.roze.nexacommerce.returns.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyCreateRequest;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyUpdateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnPolicyResponse;
import com.roze.nexacommerce.returns.service.ReturnPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/return-policies")
@RequiredArgsConstructor
@Tag(name = "Return Policies", description = "APIs for managing return policies")
public class ReturnPolicyController extends BaseController {
    
    private final ReturnPolicyService returnPolicyService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Create a new return policy")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> createPolicy(
            @Valid @RequestBody ReturnPolicyCreateRequest request) {
        ReturnPolicyResponse response = returnPolicyService.createPolicy(request);
        return created(response, "Return policy created successfully");
    }
    
    @GetMapping("/{policyId}")
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Get return policy by ID")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> getPolicyById(@PathVariable Long policyId) {
        ReturnPolicyResponse response = returnPolicyService.getPolicyById(policyId);
        return ok(response, "Return policy retrieved successfully");
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Get all return policies")
    public ResponseEntity<BaseResponse<List<ReturnPolicyResponse>>> getAllPolicies() {
        List<ReturnPolicyResponse> response = returnPolicyService.getAllPolicies();
        return ok(response, "Return policies retrieved successfully");
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active return policies")
    public ResponseEntity<BaseResponse<List<ReturnPolicyResponse>>> getActivePolicies() {
        List<ReturnPolicyResponse> response = returnPolicyService.getActivePolicies();
        return ok(response, "Active return policies retrieved successfully");
    }
    
    @GetMapping("/default")
    @Operation(summary = "Get default return policy")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> getDefaultPolicy() {
        ReturnPolicyResponse response = returnPolicyService.getDefaultPolicy();
        return ok(response, "Default return policy retrieved successfully");
    }
    
    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Update return policy")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody ReturnPolicyUpdateRequest request) {
        ReturnPolicyResponse response = returnPolicyService.updatePolicy(policyId, request);
        return ok(response, "Return policy updated successfully");
    }
    
    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Delete return policy")
    public ResponseEntity<BaseResponse<Void>> deletePolicy(@PathVariable Long policyId) {
        returnPolicyService.deletePolicy(policyId);
        return noContent("Return policy deleted successfully");
    }
    
    @PatchMapping("/{policyId}/toggle-status")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Toggle return policy status")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> togglePolicyStatus(@PathVariable Long policyId) {
        ReturnPolicyResponse response = returnPolicyService.togglePolicyStatus(policyId);
        return ok(response, "Return policy status updated successfully");
    }
    
    @PatchMapping("/{policyId}/set-default")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Set return policy as default")
    public ResponseEntity<BaseResponse<ReturnPolicyResponse>> setDefaultPolicy(@PathVariable Long policyId) {
        ReturnPolicyResponse response = returnPolicyService.setDefaultPolicy(policyId);
        return ok(response, "Return policy set as default successfully");
    }
}