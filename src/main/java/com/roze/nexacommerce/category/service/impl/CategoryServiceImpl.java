package com.roze.nexacommerce.category.service.impl;

import com.roze.nexacommerce.category.dto.request.CategoryCreateRequest;
import com.roze.nexacommerce.category.dto.request.CategoryUpdateRequest;
import com.roze.nexacommerce.category.dto.response.CategoryResponse;
import com.roze.nexacommerce.category.dto.response.CategoryTreeResponse;
import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.category.mapper.CategoryMapper;
import com.roze.nexacommerce.category.repository.CategoryRepository;
import com.roze.nexacommerce.category.service.CategoryService;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        // Validate unique constraints
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        if (request.getSlug() != null && categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Category", "slug", request.getSlug());
        }

        Category category = categoryMapper.toEntity(request);

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<CategoryResponse> getCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
//        List<CategoryResponse> categoryResponses = categoryPage.getContent().stream()
//                .map(categoryMapper::toResponse)
//                .collect(Collectors.toList());
        // Use batch mapping for better performance
        List<CategoryResponse> categoryResponses = categoryMapper.toResponseList(categoryPage.getContent());


        return PaginatedResponse.<CategoryResponse>builder()
                .items(categoryResponses)
                .totalItems(categoryPage.getTotalElements())
                .currentPage(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalPages(categoryPage.getTotalPages())
                .build();
    }

    @Override
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(categoryMapper::toTreeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getFeaturedCategories() {
        return categoryRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Validate unique constraints
        if (request.getName() != null && !request.getName().equals(category.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())
                && categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Category", "slug", request.getSlug());
        }

        categoryMapper.updateEntity(request, category);

        // Update parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Check if category has products
        Long productCount = categoryRepository.countActiveProductsByCategory(category);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }

        // Handle child categories - move them to parent or make them root
        if (!category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                child.setParent(category.getParent());
            }
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryResponse toggleCategoryStatus(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setActive(!category.getActive());
        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }
}