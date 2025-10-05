package com.roze.nexacommerce.product.dto.response;

import com.roze.nexacommerce.product.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long vendorId;
    private String vendorName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stock;
    private Integer lowStockThreshold;
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
    private Boolean inStock;
    private Boolean lowStock;
    private Boolean available;
    
    @Builder.Default
    private List<ProductImageResponse> images = new ArrayList<>();
    
    @Builder.Default
    private List<ProductAttributeResponse> attributes = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}