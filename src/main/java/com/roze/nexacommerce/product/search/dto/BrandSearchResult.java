// com.roze.nexacommerce.search.dto.BrandSearchResult.java
package com.roze.nexacommerce.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandSearchResult {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String slug;
    private Long productCount;
    private String type;
}