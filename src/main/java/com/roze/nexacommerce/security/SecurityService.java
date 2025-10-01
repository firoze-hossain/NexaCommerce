package com.roze.nexacommerce.security;

import com.roze.nexacommerce.common.address.repository.AddressRepository;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;
    private final VendorProfileRepository vendorRepository;
    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerRepository;

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getEmail().equals(currentUsername);
    }

    public boolean canAccessCustomer(Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();

        // Check if user has READ_CUSTOMER permission (for admin/superadmin)
        boolean hasReadPermission = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("READ_CUSTOMER"));

        if (hasReadPermission) {
            return true;
        }

        // Check if user is accessing their own customer data
        return customerRepository.findById(customerId)
                .map(customer -> customer.getUser().getEmail().equals(currentUsername))
                .orElse(false);
    }

    public boolean isVendorOwner(Long vendorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        return vendorRepository.findById(vendorId)
                .map(vendor -> vendor.getUser().getEmail().equals(currentUsername))
                .orElse(false);
    }

    public boolean isAddressOwner(Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        return addressRepository.findById(addressId)
                .map(address -> address.getUser().getEmail().equals(currentUsername))
                .orElse(false);
    }
}