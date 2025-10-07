package com.roze.nexacommerce.brand.service.impl;

import com.roze.nexacommerce.brand.dto.request.BrandCreateRequest;
import com.roze.nexacommerce.brand.dto.request.BrandUpdateRequest;
import com.roze.nexacommerce.brand.dto.response.BrandResponse;
import com.roze.nexacommerce.brand.dto.response.BrandWithProductsResponse;
import com.roze.nexacommerce.brand.entity.Brand;
import com.roze.nexacommerce.brand.mapper.BrandMapper;
import com.roze.nexacommerce.brand.repository.BrandRepository;
import com.roze.nexacommerce.brand.service.BrandService;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandCreateRequest request) {
        // Validate unique constraints
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }
        
        if (request.getSlug() != null && brandRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Brand", "slug", request.getSlug());
        }

        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);
        
        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "slug", slug));
        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandWithProductsResponse getBrandWithProducts(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        return brandMapper.toResponseWithProducts(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BrandResponse> getBrands(Pageable pageable) {
        Page<Brand> brandPage = brandRepository.findAll(pageable);
        List<BrandResponse> brandResponses = brandPage.getContent().stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<BrandResponse>builder()
                .items(brandResponses)
                .totalItems(brandPage.getTotalElements())
                .currentPage(brandPage.getNumber())
                .pageSize(brandPage.getSize())
                .totalPages(brandPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrands() {
        return brandRepository.findByActiveTrue().stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getFeaturedBrands() {
        return brandRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long brandId, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));

        // Validate unique constraints
        if (request.getName() != null && !request.getName().equals(brand.getName()) 
                && brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand", "name", request.getName());
        }
        
        if (request.getSlug() != null && !request.getSlug().equals(brand.getSlug()) 
                && brandRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Brand", "slug", request.getSlug());
        }

        brandMapper.updateEntity(request, brand);
        Brand updatedBrand = brandRepository.save(brand);
        
        return brandMapper.toResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        
        // Check if brand has products
        if (brand.hasProducts()) {
            throw new IllegalStateException("Cannot delete brand with associated products");
        }
        
        brandRepository.delete(brand);
    }

    @Override
    @Transactional
    public BrandResponse toggleBrandStatus(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
        
        brand.setActive(!brand.getActive());
        Brand updatedBrand = brandRepository.save(brand);
        
        return brandMapper.toResponse(updatedBrand);
    }
}