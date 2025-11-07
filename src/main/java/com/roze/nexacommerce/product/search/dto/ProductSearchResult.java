// com.roze.nexacommerce.search.dto.ProductSearchResult.java
package com.roze.nexacommerce.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResult {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String imageUrl;
    private String categoryName;
    private String brandName;
    private String vendorName;
    private Boolean inStock;
    private String slug;
    private String type;
}