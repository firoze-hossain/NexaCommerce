package com.roze.nexacommerce.product.mapper;

import com.roze.nexacommerce.product.dto.request.ProductAttributeRequest;
import com.roze.nexacommerce.product.dto.request.ProductCreateRequest;
import com.roze.nexacommerce.product.dto.request.ProductImageRequest;
import com.roze.nexacommerce.product.dto.request.ProductUpdateRequest;
import com.roze.nexacommerce.product.dto.response.ProductAttributeResponse;
import com.roze.nexacommerce.product.dto.response.ProductImageResponse;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
import com.roze.nexacommerce.product.dto.response.VendorInfo;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.entity.ProductAttribute;
import com.roze.nexacommerce.product.entity.ProductImage;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ModelMapper modelMapper;

    public Product toEntity(ProductCreateRequest request) {
        Product product = modelMapper.map(request, Product.class);

        // Map images
        if (request.getImages() != null) {
            List<ProductImage> images = request.getImages().stream()
                    .map(this::toImageEntity)
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        // Map attributes
        if (request.getAttributes() != null) {
            List<ProductAttribute> attributes = request.getAttributes().stream()
                    .map(this::toAttributeEntity)
                    .collect(Collectors.toList());
            product.setAttributes(attributes);
        }

        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);

        // Set vendor info
        if (product.getVendor() != null) {
            response.setVendorId(product.getVendor().getId());
            response.setVendorName(product.getVendor().getCompanyName());
        }

        // Set category info
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            response.setBrandId(product.getBrand().getId());
            response.setBrandName(product.getBrand().getName());
            response.setBrandSlug(product.getBrand().getSlug());
        }
        // Map images
        if (product.getImages() != null) {
            List<ProductImageResponse> imageResponses = product.getImages().stream()
                    .map(this::toImageResponse)
                    .collect(Collectors.toList());
            response.setImages(imageResponses);
        }

        // Map attributes
        if (product.getAttributes() != null) {
            List<ProductAttributeResponse> attributeResponses = product.getAttributes().stream()
                    .map(this::toAttributeResponse)
                    .collect(Collectors.toList());
            response.setAttributes(attributeResponses);
        }

        // Set computed fields
        response.setInStock(product.isInStock());
        response.setLowStock(product.isLowStock());
        response.setAvailable(product.isAvailable());

        return response;
    }

    public ProductImage toImageEntity(ProductImageRequest request) {
        return modelMapper.map(request, ProductImage.class);
    }

    public ProductImageResponse toImageResponse(ProductImage image) {
        return modelMapper.map(image, ProductImageResponse.class);
    }

    public ProductAttribute toAttributeEntity(ProductAttributeRequest request) {
        return modelMapper.map(request, ProductAttribute.class);
    }

    public ProductAttributeResponse toAttributeResponse(ProductAttribute attribute) {
        return modelMapper.map(attribute, ProductAttributeResponse.class);
    }

    public void updateEntity(ProductUpdateRequest request, Product product) {
        modelMapper.map(request, product);
    }

    public VendorInfo toVendorInfo(VendorProfile vendor) {
        return VendorInfo.builder()
                .vendorId(vendor.getId())
                .companyName(vendor.getCompanyName())
                .businessEmail(vendor.getBusinessEmail())
                .phone(vendor.getPhone())
                .rating(vendor.getRatingAvg() != null ? vendor.getRatingAvg().doubleValue() : 0.0)
                .build();
    }
}