// com.roze.nexacommerce.search.dto.SearchResult.java
package com.roze.nexacommerce.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<ProductSearchResult> products;
    private List<CategorySearchResult> categories;
    private List<BrandSearchResult> brands;
    private SearchFilters availableFilters;
    private Long totalResults;
    private Long searchTime;
}