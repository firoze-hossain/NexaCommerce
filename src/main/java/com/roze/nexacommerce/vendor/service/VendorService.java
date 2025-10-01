package com.roze.nexacommerce.vendor.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.vendor.dto.request.VendorRegistrationRequest;
import com.roze.nexacommerce.vendor.dto.request.VendorUpdateRequest;
import com.roze.nexacommerce.vendor.dto.response.VendorDetailResponse;
import com.roze.nexacommerce.vendor.dto.response.VendorResponse;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import org.springframework.data.domain.Pageable;

public interface VendorService {
    VendorDetailResponse register(VendorRegistrationRequest request);

    VendorDetailResponse getVendorById(Long vendorId);

    VendorDetailResponse getVendorByUserId(Long userId);

    VendorDetailResponse getVendorByEmail(String email);

    PaginatedResponse<VendorResponse> getAllVendors(Pageable pageable);

    PaginatedResponse<VendorResponse> getVendorsByStatus(VendorStatus status, Pageable pageable);

    VendorDetailResponse updateVendor(Long vendorId, VendorUpdateRequest request);

    VendorDetailResponse approveVendor(Long vendorId);

    VendorDetailResponse rejectVendor(Long vendorId, String reason);

    VendorDetailResponse suspendVendor(Long vendorId, String reason);

    void deleteVendor(Long vendorId);
}