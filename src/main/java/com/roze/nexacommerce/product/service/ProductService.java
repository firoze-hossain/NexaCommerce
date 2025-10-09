package com.roze.nexacommerce.product.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.product.dto.request.ProductCreateRequest;
import com.roze.nexacommerce.product.dto.request.ProductUpdateRequest;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
import com.roze.nexacommerce.product.enums.ProductStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request, Long vendorId);

    ProductResponse getProductById(Long productId);

    ProductResponse getProductBySku(String sku);

    PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable);

    PaginatedResponse<ProductResponse> getProductsByVendor(Long vendorId, Pageable pageable);

    PaginatedResponse<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    PaginatedResponse<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable);

    PaginatedResponse<ProductResponse> searchProducts(String query, Pageable pageable);

    PaginatedResponse<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<ProductResponse> getFeaturedProducts();

    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);

    ProductResponse updateProductStatus(Long productId, ProductStatus status);

    ProductResponse updateProductStock(Long productId, Integer stock);

    void deleteProduct(Long productId);

    PaginatedResponse<ProductResponse> getProductsByBrand(Long brandId, Pageable pageable);
}