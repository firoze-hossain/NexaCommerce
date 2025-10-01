package com.roze.nexacommerce.common.address.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.common.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController extends BaseController {
    private final AddressService addressService;

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('CREATE_ADDRESS') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<AddressResponse>> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(userId, request);
        return created(response, "Address created successfully");
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getUserAddresses(@PathVariable Long userId) {
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ok(addresses, "Addresses retrieved successfully");
    }

    @GetMapping("/users/{userId}/type/{addressType}")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getUserAddressesByType(
            @PathVariable Long userId,
            @PathVariable AddressType addressType) {
        List<AddressResponse> addresses = addressService.getUserAddressesByType(userId, addressType);
        return ok(addresses, "Addresses retrieved successfully");
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isAddressOwner(#addressId)")
    public ResponseEntity<BaseResponse<AddressResponse>> getAddressById(@PathVariable Long addressId) {
        AddressResponse address = addressService.getAddressById(addressId);
        return ok(address, "Address retrieved successfully");
    }

    @GetMapping("/users/{userId}/default/{addressType}")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<AddressResponse>> getDefaultAddress(
            @PathVariable Long userId,
            @PathVariable AddressType addressType) {
        AddressResponse address = addressService.getDefaultAddress(userId, addressType);
        if (address == null) {
            return notFound("No default address found for the specified type");
        }
        return ok(address, "Default address retrieved successfully");
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasAuthority('UPDATE_ADDRESS') or @securityService.isAddressOwner(#addressId)")
    public ResponseEntity<BaseResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(addressId, request);
        return ok(response, "Address updated successfully");
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("hasAuthority('UPDATE_ADDRESS') or @securityService.isAddressOwner(#addressId)")
    public ResponseEntity<BaseResponse<AddressResponse>> setDefaultAddress(@PathVariable Long addressId) {
        AddressResponse response = addressService.setDefaultAddress(addressId);
        return ok(response, "Address set as default successfully");
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasAuthority('DELETE_ADDRESS') or @securityService.isAddressOwner(#addressId)")
    public ResponseEntity<BaseResponse<Void>> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return noContent("Address deleted successfully");
    }
}