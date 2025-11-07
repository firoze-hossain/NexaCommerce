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
public class SearchFilters {
    private List<CategoryFilter> categories;
    private List<BrandFilter> brands;
    private PriceRange priceRange;
    private Boolean inStock;
    private Boolean featured;
}