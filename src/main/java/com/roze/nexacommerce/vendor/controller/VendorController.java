package com.roze.nexacommerce.vendor.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.vendor.dto.request.VendorRegistrationRequest;
import com.roze.nexacommerce.vendor.dto.request.VendorUpdateRequest;
import com.roze.nexacommerce.vendor.dto.response.VendorDetailResponse;
import com.roze.nexacommerce.vendor.dto.response.VendorResponse;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import com.roze.nexacommerce.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController extends BaseController {
    private final VendorService vendorService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> register(@Valid @RequestBody VendorRegistrationRequest request) {
        VendorDetailResponse response = vendorService.register(request);
        return created(response, "Vendor registration submitted for approval");
    }

    @GetMapping("/{vendorId}")
    @PreAuthorize("@securityService.canAccessVendor(#vendorId)")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> getVendorById(@PathVariable Long vendorId) {
        VendorDetailResponse response = vendorService.getVendorById(vendorId);
        return ok(response, "Vendor retrieved successfully");
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('READ_VENDOR') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> getVendorByUserId(@PathVariable Long userId) {
        VendorDetailResponse response = vendorService.getVendorByUserId(userId);
        return ok(response, "Vendor retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_VENDOR')")
    public ResponseEntity<BaseResponse<PaginatedResponse<VendorResponse>>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<VendorResponse> vendors = vendorService.getAllVendors(pageable);
        return paginated(vendors, "Vendors retrieved successfully");
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('READ_VENDOR')")
    public ResponseEntity<BaseResponse<PaginatedResponse<VendorResponse>>> getVendorsByStatus(
            @RequestParam VendorStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<VendorResponse> vendors = vendorService.getVendorsByStatus(status, pageable);
        return paginated(vendors, "Vendors retrieved successfully");
    }

    @PutMapping("/{vendorId}")
    @PreAuthorize("hasAuthority('UPDATE_VENDOR') or @securityService.isVendorOwner(#vendorId)")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        VendorDetailResponse response = vendorService.updateVendor(vendorId, request);
        return ok(response, "Vendor updated successfully");
    }

    @PatchMapping("/{vendorId}/approve")
    @PreAuthorize("hasAuthority('APPROVE_VENDOR')")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> approveVendor(@PathVariable Long vendorId) {
        VendorDetailResponse response = vendorService.approveVendor(vendorId);
        return ok(response, "Vendor approved successfully");
    }

    @PatchMapping("/{vendorId}/reject")
    @PreAuthorize("hasAuthority('APPROVE_VENDOR')")
    public ResponseEntity<BaseResponse<VendorDetailResponse>> rejectVendor(
            @PathVariable Long vendorId,
            @RequestParam String reason) {
        VendorDetailResponse response = vendorService.rejectVendor(vendorId, reason);
        return ok(response, "Vendor rejected successfully");
    }

    @DeleteMapping("/{vendorId}")
    @PreAuthorize("hasAuthority('DELETE_VENDOR')")
    public ResponseEntity<BaseResponse<Void>> deleteVendor(@PathVariable Long vendorId) {
        vendorService.deleteVendor(vendorId);
        return noContent("Vendor deleted successfully");
    }
}