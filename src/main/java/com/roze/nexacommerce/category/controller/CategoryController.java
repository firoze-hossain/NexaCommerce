package com.roze.nexacommerce.category.controller;

import com.roze.nexacommerce.category.dto.request.CategoryCreateRequest;
import com.roze.nexacommerce.category.dto.request.CategoryUpdateRequest;
import com.roze.nexacommerce.category.dto.response.CategoryResponse;
import com.roze.nexacommerce.category.dto.response.CategoryTreeResponse;
import com.roze.nexacommerce.category.service.CategoryService;
import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController extends BaseController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_CATEGORY')")
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return created(response, "Category created successfully");
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ok(response, "Category retrieved successfully");
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getCategoryBySlug(slug);
        return ok(response, "Category retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResponse<CategoryResponse>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<CategoryResponse> categories = categoryService.getCategories(pageable);
        return paginated(categories, "Categories retrieved successfully");
    }

    @GetMapping("/tree")
    public ResponseEntity<BaseResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> response = categoryService.getCategoryTree();
        return ok(response, "Category tree retrieved successfully");
    }

    @GetMapping("/root")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ok(response, "Root categories retrieved successfully");
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getChildCategories(@PathVariable Long parentId) {
        List<CategoryResponse> response = categoryService.getChildCategories(parentId);
        return ok(response, "Child categories retrieved successfully");
    }

    @GetMapping("/featured")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getFeaturedCategories() {
        List<CategoryResponse> response = categoryService.getFeaturedCategories();
        return ok(response, "Featured categories retrieved successfully");
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('UPDATE_CATEGORY')")
    public ResponseEntity<BaseResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ok(response, "Category updated successfully");
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('DELETE_CATEGORY')")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return noContent("Category deleted successfully");
    }

    @PatchMapping("/{categoryId}/toggle-status")
    @PreAuthorize("hasAuthority('UPDATE_CATEGORY')")
    public ResponseEntity<BaseResponse<CategoryResponse>> toggleCategoryStatus(@PathVariable Long categoryId) {
        CategoryResponse response = categoryService.toggleCategoryStatus(categoryId);
        return ok(response, "Category status updated successfully");
    }
}