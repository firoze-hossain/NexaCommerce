package com.roze.nexacommerce.vendor.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.RoleRepository;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.vendor.dto.request.VendorRegistrationRequest;
import com.roze.nexacommerce.vendor.dto.request.VendorUpdateRequest;
import com.roze.nexacommerce.vendor.dto.response.VendorDetailResponse;
import com.roze.nexacommerce.vendor.dto.response.VendorResponse;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import com.roze.nexacommerce.vendor.mapper.VendorMapper;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import com.roze.nexacommerce.vendor.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {
    private final VendorProfileRepository vendorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VendorMapper vendorMapper;

    @Override
    @Transactional
    public VendorDetailResponse register(VendorRegistrationRequest request) {
        // Validate unique constraints
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (vendorRepository.existsByCompanyName(request.getCompanyName())) {
            throw new DuplicateResourceException("Vendor", "company_name", request.getCompanyName());
        }
        if (vendorRepository.existsByBusinessEmail(request.getBusinessEmail())) {
            throw new DuplicateResourceException("Vendor", "business_email", request.getBusinessEmail());
        }

        // Get VENDOR role
        Role vendorRole = roleRepository.findByName("VENDOR")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "VENDOR"));

        // Create user
        User user = User.builder()
                .name(request.getContactPersonName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(vendorRole)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        // Create vendor profile
        VendorProfile vendor = vendorMapper.toEntity(request, savedUser);
        VendorProfile savedVendor = vendorRepository.save(vendor);

        return vendorMapper.toDetailResponse(savedVendor);
    }

    @Override
    public VendorDetailResponse getVendorById(Long vendorId) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
        return vendorMapper.toDetailResponse(vendor);
    }

    @Override
    public VendorDetailResponse getVendorByUserId(Long userId) {
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "user_id", userId));
        return vendorMapper.toDetailResponse(vendor);
    }

    @Override
    public VendorDetailResponse getVendorByEmail(String email) {
        VendorProfile vendor = vendorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "email", email));
        return vendorMapper.toDetailResponse(vendor);
    }

    @Override
    public PaginatedResponse<VendorResponse> getAllVendors(Pageable pageable) {
        Page<VendorProfile> vendorPage = vendorRepository.findAll(pageable);
        List<VendorResponse> vendorResponses = vendorPage.getContent()
                .stream()
                .map(vendorMapper::toResponse)
                .toList();

        return PaginatedResponse.<VendorResponse>builder()
                .items(vendorResponses)
                .totalItems(vendorPage.getTotalElements())
                .currentPage(vendorPage.getNumber())
                .pageSize(vendorPage.getSize())
                .totalPages(vendorPage.getTotalPages())
                .build();
    }

    @Override
    public PaginatedResponse<VendorResponse> getVendorsByStatus(VendorStatus status, Pageable pageable) {
        Page<VendorProfile> vendorPage = vendorRepository.findByStatus(status, pageable);
        List<VendorResponse> vendorResponses = vendorPage.getContent()
                .stream()
                .map(vendorMapper::toResponse)
                .toList();

        return PaginatedResponse.<VendorResponse>builder()
                .items(vendorResponses)
                .totalItems(vendorPage.getTotalElements())
                .currentPage(vendorPage.getNumber())
                .pageSize(vendorPage.getSize())
                .totalPages(vendorPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public VendorDetailResponse updateVendor(Long vendorId, VendorUpdateRequest request) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendorMapper.updateEntity(request, vendor);
        VendorProfile updatedVendor = vendorRepository.save(vendor);

        return vendorMapper.toDetailResponse(updatedVendor);
    }

    @Override
    @Transactional
    public VendorDetailResponse approveVendor(Long vendorId) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendor.setStatus(VendorStatus.APPROVED);
        VendorProfile updatedVendor = vendorRepository.save(vendor);

        return vendorMapper.toDetailResponse(updatedVendor);
    }

    @Override
    @Transactional
    public VendorDetailResponse rejectVendor(Long vendorId, String reason) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendor.setStatus(VendorStatus.REJECTED);
        VendorProfile updatedVendor = vendorRepository.save(vendor);

        return vendorMapper.toDetailResponse(updatedVendor);
    }

    @Override
    @Transactional
    public VendorDetailResponse suspendVendor(Long vendorId, String reason) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendor.setStatus(VendorStatus.SUSPENDED);
        VendorProfile updatedVendor = vendorRepository.save(vendor);

        return vendorMapper.toDetailResponse(updatedVendor);
    }

    @Override
    @Transactional
    public void deleteVendor(Long vendorId) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        // Also delete the associated user
        userRepository.delete(vendor.getUser());
        vendorRepository.delete(vendor);
    }
}