
package com.roze.nexacommerce.product.search.service.impl;

import com.roze.nexacommerce.brand.entity.Brand;
import com.roze.nexacommerce.brand.repository.BrandRepository;
import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.category.repository.CategoryRepository;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.entity.ProductImage;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.product.search.dto.*;
import com.roze.nexacommerce.product.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

//    @Override
//    @Transactional(readOnly = true)
//    public SearchResult search(SearchRequest request, Pageable pageable) {
//        long startTime = System.currentTimeMillis();
//
//        // Search products with filters
//        Page<Product> productPage = searchProductsWithFilters(request, pageable);
//        List<ProductSearchResult> productResults = productPage.getContent().stream()
//                .map(this::mapToProductSearchResult)
//                .collect(Collectors.toList());
//
//        // Search categories
//        List<CategorySearchResult> categoryResults = searchCategories(request.getQuery());
//
//        // Search brands
//        List<BrandSearchResult> brandResults = searchBrands(request.getQuery());
//
//        long searchTime = System.currentTimeMillis() - startTime;
//
//        return SearchResult.builder()
//                .products(productResults)
//                .categories(categoryResults)
//                .brands(brandResults)
//                .totalResults(productPage.getTotalElements() + categoryResults.size() + brandResults.size())
//                .searchTime(searchTime)
//                .build();
//    }
@Override
@Transactional(readOnly = true)
public SearchResult search(SearchRequest request, Pageable pageable) {
    long startTime = System.currentTimeMillis();

    // Search products with filters
    Page<Product> productPage = searchProductsWithFilters(request, pageable);
    List<ProductSearchResult> productResults = productPage.getContent().stream()
            .map(this::mapToProductSearchResult)
            .collect(Collectors.toList());

    // Search categories - always show relevant categories
    List<CategorySearchResult> categoryResults = searchCategories(request.getQuery());

    // Search brands - always show relevant brands
    List<BrandSearchResult> brandResults = searchBrands(request.getQuery());

    // Get available filters based on current results
    SearchFilters availableFilters = getAvailableFilters(request, productPage.getContent());

    long searchTime = System.currentTimeMillis() - startTime;

    return SearchResult.builder()
            .products(productResults)
            .categories(categoryResults)
            .brands(brandResults)
            .availableFilters(availableFilters)
            .totalResults(productPage.getTotalElements())
            .searchTime(searchTime)
            .build();
}

    private SearchFilters getAvailableFilters(SearchRequest request, List<Product> products) {
        if (products.isEmpty()) {
            return SearchFilters.builder().build();
        }

        // Extract available categories from products
        Set<Long> availableCategoryIds = products.stream()
                .map(product -> product.getCategory().getId())
                .collect(Collectors.toSet());
        List<CategoryFilter> availableCategories = categoryRepository.findByIdIn(availableCategoryIds)
                .stream()
                .map(category -> CategoryFilter.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .productCount(products.stream()
                                .filter(p -> p.getCategory().getId().equals(category.getId()))
                                .count())
                        .build())
                .collect(Collectors.toList());

        // Extract available brands from products
        Set<Long> availableBrandIds = products.stream()
                .filter(product -> product.getBrand() != null)
                .map(product -> product.getBrand().getId())
                .collect(Collectors.toSet());
        List<BrandFilter> availableBrands = brandRepository.findByIdIn(availableBrandIds)
                .stream()
                .map(brand -> BrandFilter.builder()
                        .id(brand.getId())
                        .name(brand.getName())
                        .productCount(products.stream()
                                .filter(p -> p.getBrand() != null && p.getBrand().getId().equals(brand.getId()))
                                .count())
                        .build())
                .collect(Collectors.toList());

        // Calculate price range
        DoubleSummaryStatistics priceStats = products.stream()
                .mapToDouble(product -> product.getPrice().doubleValue())
                .summaryStatistics();

        return SearchFilters.builder()
                .categories(availableCategories)
                .brands(availableBrands)
                .priceRange(PriceRange.builder()
                        .min(BigDecimal.valueOf(priceStats.getMin()))
                        .max(BigDecimal.valueOf(priceStats.getMax()))
                        .build())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public AutocompleteResult autocomplete(String query, int limit) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return AutocompleteResult.builder()
                    .products(new ArrayList<>())
                    .categories(new ArrayList<>())
                    .brands(new ArrayList<>())
                    .popularSearches(new ArrayList<>())
                    .build();
        }

        // Get limited results for autocomplete
        List<Product> products = productRepository.searchProductsAutocomplete(query, limit);
        List<Category> categories = categoryRepository.searchCategoriesAutocomplete(query, limit);
        List<Brand> brands = brandRepository.searchBrandsAutocomplete(query, limit);

        List<ProductSearchResult> productResults = products.stream()
                .map(this::mapToProductSearchResult)
                .collect(Collectors.toList());

        List<CategorySearchResult> categoryResults = categories.stream()
                .map(this::mapToCategorySearchResult)
                .collect(Collectors.toList());

        List<BrandSearchResult> brandResults = brands.stream()
                .map(this::mapToBrandSearchResult)
                .collect(Collectors.toList());

        List<String> popularSearches = getPopularSearches(5);

        return AutocompleteResult.builder()
                .products(productResults)
                .categories(categoryResults)
                .brands(brandResults)
                .popularSearches(popularSearches)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPopularSearches(int limit) {
        // In a real implementation, you might want to track popular searches
        // For now, return some default popular searches
        return List.of("laptop", "smartphone", "headphones", "watch", "camera");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String query) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return new ArrayList<>();
        }

        // Get suggestions from product names, categories, and brands
        List<String> suggestions = new ArrayList<>();
        
        // Product name suggestions
        List<String> productSuggestions = productRepository.findProductNameSuggestions(query, 3);
        suggestions.addAll(productSuggestions);

        // Category suggestions
        List<String> categorySuggestions = categoryRepository.findCategoryNameSuggestions(query, 2);
        suggestions.addAll(categorySuggestions);

        // Brand suggestions
        List<String> brandSuggestions = brandRepository.findBrandNameSuggestions(query, 2);
        suggestions.addAll(brandSuggestions);

        return suggestions.stream().distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductSearchResult> searchProductsOnly(String query, Pageable pageable, String sortBy) {
        Page<Product> productPage = searchProductsWithFilters(
                SearchRequest.builder().query(query).sortBy(sortBy).build(),
                pageable
        );

        List<ProductSearchResult> productResults = productPage.getContent().stream()
                .map(this::mapToProductSearchResult)
                .collect(Collectors.toList());

        return PaginatedResponse.<ProductSearchResult>builder()
                .items(productResults)
                .totalItems(productPage.getTotalElements())
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    private Page<Product> searchProductsWithFilters(SearchRequest request, Pageable pageable) {
        // Apply sorting based on request
        Pageable sortedPageable = applySorting(pageable, request.getSortBy());

        return productRepository.searchWithFilters(
                request.getQuery(),
                request.getCategories(),
                request.getBrands(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getInStock(),
                request.getFeatured(),
                sortedPageable
        );
    }

    private Pageable applySorting(Pageable pageable, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "price_asc":
                sort = Sort.by("price").ascending();
                break;
            case "price_desc":
                sort = Sort.by("price").descending();
                break;
            case "name":
                sort = Sort.by("name").ascending();
                break;
            case "newest":
                sort = Sort.by("createdAt").descending();
                break;
            case "relevance":
            default:
                // For relevance, we might want to use a different approach
                // For now, sort by name for relevance
                sort = Sort.by("name").ascending();
                break;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private List<CategorySearchResult> searchCategories(String query) {
        if (!StringUtils.hasText(query)) {
            return new ArrayList<>();
        }

        List<Category> categories = categoryRepository.searchCategories(query, 5);
        return categories.stream()
                .map(this::mapToCategorySearchResult)
                .collect(Collectors.toList());
    }

    private List<BrandSearchResult> searchBrands(String query) {
        if (!StringUtils.hasText(query)) {
            return new ArrayList<>();
        }

        List<Brand> brands = brandRepository.searchBrands(query, 5);
        return brands.stream()
                .map(this::mapToBrandSearchResult)
                .collect(Collectors.toList());
    }

    private ProductSearchResult mapToProductSearchResult(Product product) {
        String primaryImageUrl = product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().stream()
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(null));

        return ProductSearchResult.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .imageUrl(primaryImageUrl)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .vendorName(product.getVendor() != null ? product.getVendor().getCompanyName() : null)
                .inStock(product.isInStock())
                .type("PRODUCT")
                .build();
    }

    private CategorySearchResult mapToCategorySearchResult(Category category) {
        // Count active products in category (you might want to cache this)
        long productCount = category.getProducts().stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE && product.getPublished())
                .count();

        return CategorySearchResult.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .productCount(productCount)
                .type("CATEGORY")
                .build();
    }

    private BrandSearchResult mapToBrandSearchResult(Brand brand) {
        // Count active products for brand
        long productCount = brand.getProducts().stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE && product.getPublished())
                .count();

        return BrandSearchResult.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .productCount(productCount)
                .type("BRAND")
                .build();
    }
}