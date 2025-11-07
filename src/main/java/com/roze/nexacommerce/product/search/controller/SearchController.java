package com.roze.nexacommerce.product.search.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.product.search.dto.AutocompleteResult;
import com.roze.nexacommerce.product.search.dto.ProductSearchResult;
import com.roze.nexacommerce.product.search.dto.SearchRequest;
import com.roze.nexacommerce.product.search.dto.SearchResult;
import com.roze.nexacommerce.product.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController extends BaseController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<BaseResponse<SearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "relevance") String sortBy) {

        SearchRequest request = SearchRequest.builder()
                .query(q)
                .categories(categories)
                .brands(brands)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .featured(featured)
                .sortBy(sortBy)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        SearchResult result = searchService.search(request, pageable);
        return ok(result, "Search completed successfully");
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<BaseResponse<AutocompleteResult>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {

        AutocompleteResult result = searchService.autocomplete(q, limit);
        return ok(result, "Autocomplete results retrieved");
    }

    @GetMapping("/popular")
    public ResponseEntity<BaseResponse<List<String>>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit) {

        List<String> popularSearches = searchService.getPopularSearches(limit);
        return ok(popularSearches, "Popular searches retrieved");
    }

    @GetMapping("/suggestions")
    public ResponseEntity<BaseResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q) {

        List<String> suggestions = searchService.getSearchSuggestions(q);
        return ok(suggestions, "Search suggestions retrieved");
    }

    @GetMapping("/products")
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductSearchResult>>> searchProductsOnly(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "relevance") String sortBy) {

        Pageable pageable = PageRequest.of(page, size);
        PaginatedResponse<ProductSearchResult> result = searchService.searchProductsOnly(q, pageable, sortBy);
        return paginated(result, "Products search completed");
    }
}