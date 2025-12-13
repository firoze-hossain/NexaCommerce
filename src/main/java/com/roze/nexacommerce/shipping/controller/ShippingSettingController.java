package com.roze.nexacommerce.shipping.controller;

import com.roze.nexacommerce.shipping.dto.request.ShippingSettingRequest;
import com.roze.nexacommerce.shipping.dto.response.ShippingSettingResponse;
import com.roze.nexacommerce.shipping.service.ShippingSettingService;
import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingSettingController extends BaseController {
    private final ShippingSettingService shippingService;

    @GetMapping("/calculate")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> calculateShippingCost(
            @RequestParam String locationType,
            @RequestParam BigDecimal orderTotal) {
        ShippingSettingResponse response = shippingService.calculateShippingCost(locationType, orderTotal);
        return ok(response, "Shipping cost calculated successfully");
    }

    @GetMapping("/settings/{id}")
    @PreAuthorize("hasAuthority('READ_SHIPPING')")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> getShippingSettingById(@PathVariable Long id) {
        ShippingSettingResponse response = shippingService.getShippingSettingById(id);
        return ok(response, "Shipping setting retrieved successfully");
    }

    @GetMapping("/settings/location/{locationType}")
    @PreAuthorize("hasAuthority('READ_SHIPPING')")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> getShippingSettingByLocationType(
            @PathVariable String locationType) {
        ShippingSettingResponse response = shippingService.getShippingSettingByLocationType(locationType);
        return ok(response, "Shipping setting retrieved successfully");
    }

    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('READ_SHIPPING')")
    public ResponseEntity<BaseResponse<PaginatedResponse<ShippingSettingResponse>>> getShippingSettings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "locationType") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<ShippingSettingResponse> settings = shippingService.getShippingSettings(pageable);
        return paginated(settings, "Shipping settings retrieved successfully");
    }

    @GetMapping("/settings/active")
    public ResponseEntity<BaseResponse<List<ShippingSettingResponse>>> getActiveShippingSettings() {
        List<ShippingSettingResponse> response = shippingService.getActiveShippingSettings();
        return ok(response, "Active shipping settings retrieved successfully");
    }

    @GetMapping("/location-types")
    public ResponseEntity<BaseResponse<List<String>>> getAvailableLocationTypes() {
        List<String> response = shippingService.getAvailableLocationTypes();
        return ok(response, "Available location types retrieved successfully");
    }

    @PostMapping("/settings")
    @PreAuthorize("hasAuthority('CREATE_SHIPPING')")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> createShippingSetting(
            @Valid @RequestBody ShippingSettingRequest request) {
        ShippingSettingResponse response = shippingService.createShippingSetting(request);
        return created(response, "Shipping setting created successfully");
    }

    @PutMapping("/settings/{id}")
    @PreAuthorize("hasAuthority('UPDATE_SHIPPING')")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> updateShippingSetting(
            @PathVariable Long id,
            @Valid @RequestBody ShippingSettingRequest request) {
        ShippingSettingResponse response = shippingService.updateShippingSetting(id, request);
        return ok(response, "Shipping setting updated successfully");
    }

    @DeleteMapping("/settings/{id}")
    @PreAuthorize("hasAuthority('DELETE_SHIPPING')")
    public ResponseEntity<BaseResponse<Void>> deleteShippingSetting(@PathVariable Long id) {
        shippingService.deleteShippingSetting(id);
        return noContent("Shipping setting deleted successfully");
    }

    @PatchMapping("/settings/{id}/toggle-status")
    @PreAuthorize("hasAuthority('UPDATE_SHIPPING')")
    public ResponseEntity<BaseResponse<ShippingSettingResponse>> toggleShippingSettingStatus(@PathVariable Long id) {
        ShippingSettingResponse response = shippingService.toggleShippingSettingStatus(id);
        return ok(response, "Shipping setting status updated successfully");
    }
}