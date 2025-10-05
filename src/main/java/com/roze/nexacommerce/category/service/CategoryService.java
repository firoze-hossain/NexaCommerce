package com.roze.nexacommerce.category.service;

import com.roze.nexacommerce.category.dto.request.CategoryCreateRequest;
import com.roze.nexacommerce.category.dto.request.CategoryUpdateRequest;
import com.roze.nexacommerce.category.dto.response.CategoryResponse;
import com.roze.nexacommerce.category.dto.response.CategoryTreeResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest request);
    
    CategoryResponse getCategoryById(Long categoryId);
    
    CategoryResponse getCategoryBySlug(String slug);
    
    List<CategoryResponse> getAllCategories();
    
    PaginatedResponse<CategoryResponse> getCategories(Pageable pageable);
    
    List<CategoryTreeResponse> getCategoryTree();
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getChildCategories(Long parentId);
    
    List<CategoryResponse> getFeaturedCategories();
    
    CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request);
    
    void deleteCategory(Long categoryId);
    
    CategoryResponse toggleCategoryStatus(Long categoryId);
}