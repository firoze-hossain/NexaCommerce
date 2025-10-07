package com.roze.nexacommerce.brand.mapper;

import com.roze.nexacommerce.brand.dto.request.BrandCreateRequest;
import com.roze.nexacommerce.brand.dto.request.BrandUpdateRequest;
import com.roze.nexacommerce.brand.dto.response.BrandResponse;
import com.roze.nexacommerce.brand.dto.response.BrandWithProductsResponse;
import com.roze.nexacommerce.brand.dto.response.ProductSummaryResponse;
import com.roze.nexacommerce.brand.entity.Brand;
import com.roze.nexacommerce.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BrandMapper {
    private final ModelMapper modelMapper;

    public Brand toEntity(BrandCreateRequest request) {
        return modelMapper.map(request, Brand.class);
    }

    public BrandResponse toResponse(Brand brand) {
        BrandResponse response = modelMapper.map(brand, BrandResponse.class);
        response.setProductCount(brand.getProducts() != null ? brand.getProducts().size() : 0);
        return response;
    }

    public BrandWithProductsResponse toResponseWithProducts(Brand brand) {
        BrandWithProductsResponse response = modelMapper.map(brand, BrandWithProductsResponse.class);

        if (brand.getProducts() != null) {
            List<ProductSummaryResponse> productSummaries =
                    brand.getProducts().stream()
                            .map(this::toProductSummary)
                            .collect(Collectors.toList());
            response.setProducts(productSummaries);
        }

        return response;
    }

    private ProductSummaryResponse toProductSummary(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .published(product.getPublished())
                .build();
    }

    public void updateEntity(BrandUpdateRequest request, Brand brand) {
        modelMapper.map(request, brand);
    }
}