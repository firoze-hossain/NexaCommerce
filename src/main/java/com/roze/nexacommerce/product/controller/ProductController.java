package com.roze.nexacommerce.product.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.product.dto.request.ProductCreateRequest;
import com.roze.nexacommerce.product.dto.request.ProductUpdateRequest;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.product.service.ProductService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT') or @securityService.isVendorUser()")
    public ResponseEntity<BaseResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestParam(required = false) Long vendorId) {
        ProductResponse response = productService.createProduct(request, vendorId);
        return created(response, "Product created successfully");
    }

    @GetMapping("/{productId}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProductById(@PathVariable Long productId) {
        ProductResponse response = productService.getProductById(productId);
        return ok(response, "Product retrieved successfully");
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        ProductResponse response = productService.getProductBySku(sku);
        return ok(response, "Product retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<ProductResponse> products = productService.getAllProducts(pageable);
        return paginated(products, "Products retrieved successfully");
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> getProductsByVendor(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<ProductResponse> products = productService.getProductsByVendor(vendorId, pageable);
        return paginated(products, "Vendor products retrieved successfully");
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return paginated(products, "Category products retrieved successfully");
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> getProductsByStatus(
            @RequestParam ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<ProductResponse> products = productService.getProductsByStatus(status, pageable);
        return paginated(products, "Products retrieved successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<ProductResponse> products = productService.searchProducts(q, pageable);
        return paginated(products, "Search results retrieved successfully");
    }

    @GetMapping("/price-range")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductResponse>>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        PaginatedResponse<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return paginated(products, "Products retrieved successfully");
    }

    @GetMapping("/featured")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getFeaturedProducts() {
        List<ProductResponse> response = productService.getFeaturedProducts();
        return ok(response, "Featured products retrieved successfully");
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or @securityService.isProductOwner(#productId)")
    public ResponseEntity<BaseResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ok(response, "Product updated successfully");
    }

    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or @securityService.isProductOwner(#productId)")
    public ResponseEntity<BaseResponse<ProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam ProductStatus status) {
        ProductResponse response = productService.updateProductStatus(productId, status);
        return ok(response, "Product status updated successfully");
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or @securityService.isProductOwner(#productId)")
    public ResponseEntity<BaseResponse<ProductResponse>> updateProductStock(
            @PathVariable Long productId,
            @RequestParam Integer stock) {
        ProductResponse response = productService.updateProductStock(productId, stock);
        return ok(response, "Product stock updated successfully");
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT') or @securityService.isProductOwner(#productId)")
    public ResponseEntity<BaseResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return noContent("Product deleted successfully");
    }
}