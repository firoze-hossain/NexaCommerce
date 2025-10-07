package com.roze.nexacommerce.brand.controller;

import com.roze.nexacommerce.brand.dto.request.BrandCreateRequest;
import com.roze.nexacommerce.brand.dto.request.BrandUpdateRequest;
import com.roze.nexacommerce.brand.dto.response.BrandResponse;
import com.roze.nexacommerce.brand.dto.response.BrandWithProductsResponse;
import com.roze.nexacommerce.brand.service.BrandService;
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

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController extends BaseController {
    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_BRAND')")
    public ResponseEntity<BaseResponse<BrandResponse>> createBrand(@Valid @RequestBody BrandCreateRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return created(response, "Brand created successfully");
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<BaseResponse<BrandResponse>> getBrandById(@PathVariable Long brandId) {
        BrandResponse response = brandService.getBrandById(brandId);
        return ok(response, "Brand retrieved successfully");
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BaseResponse<BrandResponse>> getBrandBySlug(@PathVariable String slug) {
        BrandResponse response = brandService.getBrandBySlug(slug);
        return ok(response, "Brand retrieved successfully");
    }

    @GetMapping("/{brandId}/with-products")
    public ResponseEntity<BaseResponse<BrandWithProductsResponse>> getBrandWithProducts(@PathVariable Long brandId) {
        BrandWithProductsResponse response = brandService.getBrandWithProducts(brandId);
        return ok(response, "Brand with products retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<BrandResponse>>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<BrandResponse> brands = brandService.getBrands(pageable);
        return paginated(brands, "Brands retrieved successfully");
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<BrandResponse>>> getActiveBrands() {
        List<BrandResponse> response = brandService.getActiveBrands();
        return ok(response, "Active brands retrieved successfully");
    }

    @GetMapping("/featured")
    public ResponseEntity<BaseResponse<List<BrandResponse>>> getFeaturedBrands() {
        List<BrandResponse> response = brandService.getFeaturedBrands();
        return ok(response, "Featured brands retrieved successfully");
    }

    @PutMapping("/{brandId}")
    @PreAuthorize("hasAuthority('UPDATE_BRAND')")
    public ResponseEntity<BaseResponse<BrandResponse>> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandUpdateRequest request) {
        BrandResponse response = brandService.updateBrand(brandId, request);
        return ok(response, "Brand updated successfully");
    }

    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasAuthority('DELETE_BRAND')")
    public ResponseEntity<BaseResponse<Void>> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return noContent("Brand deleted successfully");
    }

    @PatchMapping("/{brandId}/toggle-status")
    @PreAuthorize("hasAuthority('UPDATE_BRAND')")
    public ResponseEntity<BaseResponse<BrandResponse>> toggleBrandStatus(@PathVariable Long brandId) {
        BrandResponse response = brandService.toggleBrandStatus(brandId);
        return ok(response, "Brand status updated successfully");
    }
}