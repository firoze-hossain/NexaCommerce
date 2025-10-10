package com.roze.nexacommerce.category.mapper;

import com.roze.nexacommerce.category.dto.request.CategoryCreateRequest;
import com.roze.nexacommerce.category.dto.request.CategoryUpdateRequest;
import com.roze.nexacommerce.category.dto.response.CategoryResponse;
import com.roze.nexacommerce.category.dto.response.CategoryTreeResponse;
import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryMapper {
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    public Category toEntity(CategoryCreateRequest request) {
        return modelMapper.map(request, Category.class);
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = modelMapper.map(category, CategoryResponse.class);

        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }

        response.setChildrenCount(category.getChildren() != null ? category.getChildren().size() : 0);
        response.setProductCount(getProductCount(category));
        return response;
    }

    public CategoryTreeResponse toTreeResponse(Category category) {
        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .imageUrl(category.getImageUrl())
                .children(mapToTreeResponse(category.getChildren()))
                .build();
    }

    private List<CategoryTreeResponse> mapToTreeResponse(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }

        return categories.stream()
                .map(this::toTreeResponse)
                .collect(Collectors.toList());
    }

    public void updateEntity(CategoryUpdateRequest request, Category category) {
        modelMapper.map(request, category);
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        // First get all product counts in batch
        Map<Long, Long> productCounts = new HashMap<>();
        for (Category category : categories) {
            Long count = categoryRepository.countActiveProductsByCategory(category);
            productCounts.put(category.getId(), count != null ? count : 0L);
        }

        // Then map each category with pre-calculated count
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = modelMapper.map(category, CategoryResponse.class);

                    if (category.getParent() != null) {
                        response.setParentId(category.getParent().getId());
                        response.setParentName(category.getParent().getName());
                    }

                    response.setChildrenCount(category.getChildren() != null ? category.getChildren().size() : 0);
                    response.setProductCount(productCounts.get(category.getId()).intValue());

                    return response;
                })
                .collect(Collectors.toList());
    }

    private Integer getProductCount(Category category) {
        Long count = categoryRepository.countActiveProductsByCategory(category);
        return count != null ? count.intValue() : 0;
    }
}