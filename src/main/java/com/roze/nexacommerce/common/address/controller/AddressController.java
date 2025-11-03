package com.roze.nexacommerce.common.address.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.dto.response.LocationDataResponse;
import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.security.SecurityService;
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
    private final SecurityService securityService;

    // ADD THESE NEW ENDPOINTS FOR CURRENT USER
    @PostMapping("/users/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<AddressResponse>> createAddressForCurrentUser(
            @Valid @RequestBody AddressRequest request) {
        Long currentUserId = getCurrentUserId();
        AddressResponse response = addressService.createAddress(currentUserId, request);
        return created(response, "Address created successfully");
    }

    @GetMapping("/users/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getCurrentUserAddresses() {
        Long currentUserId = getCurrentUserId();
        List<AddressResponse> addresses = addressService.getUserAddresses(currentUserId);
        return ok(addresses, "Addresses retrieved successfully");
    }

    @GetMapping("/users/current/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<AddressResponse>> getCurrentUserDefaultAddress() {
        Long currentUserId = getCurrentUserId();
        AddressResponse address = addressService.getDefaultAddress(currentUserId);
        if (address == null) {
            return notFound("No default address found");
        }
        return ok(address, "Default address retrieved successfully");
    }

    @GetMapping("/users/current/type/{addressType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getCurrentUserAddressesByType(
            @PathVariable AddressType addressType) {
        Long currentUserId = getCurrentUserId();
        List<AddressResponse> addresses = addressService.getUserAddressesByType(currentUserId, addressType);
        return ok(addresses, "Addresses retrieved successfully");
    }

    @PostMapping("/admin/customers/{customerId}")
    @PreAuthorize("hasAuthority('CREATE_ADDRESS')")
    public ResponseEntity<BaseResponse<AddressResponse>> createAddressForCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddressForCustomer(customerId, request);
        return created(response, "Address created successfully for customer");
    }
    @GetMapping("/admin/customers/{customerId}")
    @PreAuthorize("hasAuthority('READ_ADDRESS')")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getCustomerAddresses(
            @PathVariable Long customerId) {
        List<AddressResponse> addresses = addressService.getCustomerAddresses(customerId);
        return ok(addresses, "Customer addresses retrieved successfully");
    }

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

    @GetMapping("/{addressId}")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isAddressOwner(#addressId)")
    public ResponseEntity<BaseResponse<AddressResponse>> getAddressById(@PathVariable Long addressId) {
        AddressResponse address = addressService.getAddressById(addressId);
        return ok(address, "Address retrieved successfully");
    }

    @GetMapping("/users/{userId}/default")
    @PreAuthorize("hasAuthority('READ_ADDRESS') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<AddressResponse>> getDefaultAddress(@PathVariable Long userId) {
        AddressResponse address = addressService.getDefaultAddress(userId);
        if (address == null) {
            return notFound("No default address found");
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

    // PUBLIC ENDPOINTS
    @GetMapping("/bangladesh/areas")
    public ResponseEntity<BaseResponse<List<String>>> getPopularAreas() {
        List<String> areas = List.of(
                "Mirpur", "Dhanmondi", "Gulshan", "Banani", "Uttara", "Mohakhali",
                "Motijheel", "Farmgate", "Malibagh", "Shyamoli", "Mohammadpur",
                "Old Dhaka", "Basundhara", "Baridhara", "Rampura", "Badda"
        );
        return ok(areas, "Popular areas retrieved successfully");
    }

    @GetMapping("/bangladesh/cities")
    public ResponseEntity<BaseResponse<List<String>>> getBangladeshCities() {
        List<String> cities = List.of(
                "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna",
                "Barisal", "Rangpur", "Mymensingh", "Comilla", "Narayanganj"
        );
        return ok(cities, "Bangladesh cities retrieved successfully");
    }

    @GetMapping("/bangladesh/location-data")
    public ResponseEntity<BaseResponse<LocationDataResponse>> getLocationData() {
        LocationDataResponse locationData = addressService.getLocationData();
        return ok(locationData, "Location data retrieved successfully");
    }

    private Long getCurrentUserId() {
        return securityService.getCurrentUserId();
    }
}