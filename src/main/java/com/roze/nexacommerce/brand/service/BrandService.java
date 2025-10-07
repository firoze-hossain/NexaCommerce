package com.roze.nexacommerce.brand.service;

import com.roze.nexacommerce.brand.dto.request.BrandCreateRequest;
import com.roze.nexacommerce.brand.dto.request.BrandUpdateRequest;
import com.roze.nexacommerce.brand.dto.response.BrandResponse;
import com.roze.nexacommerce.brand.dto.response.BrandWithProductsResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandCreateRequest request);
    
    BrandResponse getBrandById(Long brandId);
    
    BrandResponse getBrandBySlug(String slug);
    
    BrandWithProductsResponse getBrandWithProducts(Long brandId);
    
    List<BrandResponse> getAllBrands();
    
    PaginatedResponse<BrandResponse> getBrands(Pageable pageable);
    
    List<BrandResponse> getActiveBrands();
    
    List<BrandResponse> getFeaturedBrands();
    
    BrandResponse updateBrand(Long brandId, BrandUpdateRequest request);
    
    void deleteBrand(Long brandId);
    
    BrandResponse toggleBrandStatus(Long brandId);
}