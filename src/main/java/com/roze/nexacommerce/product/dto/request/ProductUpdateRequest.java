package com.roze.nexacommerce.product.dto.request;

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
public class ProductUpdateRequest {
    private Long categoryId;
    private Long brandId;
    private String name;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stock;
    private Integer lowStockThreshold;
    private String barcode;
    private Boolean trackQuantity;
    private Boolean allowBackorder;
    private BigDecimal weight;
    private String sku;
    private String weightUnit;
    private com.roze.nexacommerce.product.enums.ProductStatus status;
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