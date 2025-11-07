// com.roze.nexacommerce.search.dto.CategorySearchResult.java
package com.roze.nexacommerce.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchResult {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String slug;
    private Long productCount;
    private String type;
}