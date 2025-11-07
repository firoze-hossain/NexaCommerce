
package com.roze.nexacommerce.product.search.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.product.search.dto.AutocompleteResult;
import com.roze.nexacommerce.product.search.dto.ProductSearchResult;
import com.roze.nexacommerce.product.search.dto.SearchRequest;
import com.roze.nexacommerce.product.search.dto.SearchResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {

    SearchResult search(SearchRequest request, Pageable pageable);

    AutocompleteResult autocomplete(String query, int limit);

    List<String> getPopularSearches(int limit);

    List<String> getSearchSuggestions(String query);

    PaginatedResponse<ProductSearchResult> searchProductsOnly(String query, Pageable pageable, String sortBy);
}