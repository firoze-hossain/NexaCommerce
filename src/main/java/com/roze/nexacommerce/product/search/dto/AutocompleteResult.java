// com.roze.nexacommerce.search.dto.AutocompleteResult.java
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
public class AutocompleteResult {
    private List<ProductSearchResult> products;
    private List<CategorySearchResult> categories;
    private List<BrandSearchResult> brands;
    private List<String> popularSearches;
}