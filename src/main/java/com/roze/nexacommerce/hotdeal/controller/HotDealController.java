// HotDealController.java
package com.roze.nexacommerce.hotdeal.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealRequest;
import com.roze.nexacommerce.hotdeal.dto.request.HotDealUpdateRequest;
import com.roze.nexacommerce.hotdeal.dto.response.HotDealResponse;
import com.roze.nexacommerce.hotdeal.service.HotDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hot-deals")
@RequiredArgsConstructor
public class HotDealController extends BaseController {

    private final HotDealService hotDealService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    public ResponseEntity<BaseResponse<HotDealResponse>> createHotDeal(
            @Valid @RequestBody HotDealRequest request) {
        HotDealResponse response = hotDealService.createHotDeal(request);
        return created(response, "Hot deal created successfully");
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<HotDealResponse>>> getAllHotDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<HotDealResponse> response = hotDealService.getAllHotDeals(pageable);
        return paginated(response, "Hot deals retrieved successfully");
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<HotDealResponse>>> getActiveHotDeals() {
        List<HotDealResponse> response = hotDealService.getActiveHotDeals();
        return ok(response, "Active hot deals retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<HotDealResponse>> getHotDealById(@PathVariable Long id) {
        HotDealResponse response = hotDealService.getHotDealById(id);
        return ok(response, "Hot deal retrieved successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<BaseResponse<HotDealResponse>> updateHotDeal(
            @PathVariable Long id,
            @Valid @RequestBody HotDealUpdateRequest request) {
        HotDealResponse response = hotDealService.updateHotDeal(id, request);
        return ok(response, "Hot deal updated successfully");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<BaseResponse<HotDealResponse>> updateHotDealStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        HotDealResponse response = hotDealService.updateHotDealStatus(id, isActive);
        return ok(response, "Hot deal status updated successfully");
    }

    @PatchMapping("/{id}/increment-sold")
    public ResponseEntity<BaseResponse<HotDealResponse>> incrementSoldCount(@PathVariable Long id) {
        HotDealResponse response = hotDealService.incrementSoldCount(id);
        return ok(response, "Sold count incremented successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    public ResponseEntity<BaseResponse<Void>> deleteHotDeal(@PathVariable Long id) {
        hotDealService.deleteHotDeal(id);
        return noContent("Hot deal deleted successfully");
    }
}