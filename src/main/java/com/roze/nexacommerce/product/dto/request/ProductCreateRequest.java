package com.roze.nexacommerce.product.dto.request;

import com.roze.nexacommerce.product.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    private Long brandId;
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;
    
    private String description;
    
    private String shortDescription;
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    private BigDecimal price;
    
    private BigDecimal compareAtPrice;
    
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be positive or zero")
    private Integer stock;
    
    private Integer lowStockThreshold;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    private String barcode;
    
    private Boolean trackQuantity;
    
    private Boolean allowBackorder;
    
    private BigDecimal weight;
    
    private String weightUnit;
    
    private ProductStatus status;
    
    private Boolean featured;
    
    private Boolean published;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String tags;
    
    @Builder.Default
    private List<ProductImageRequest> images = new ArrayList<>();
    
    @Builder.Default
    private List<ProductAttributeRequest> attributes = new ArrayList<>();
}













