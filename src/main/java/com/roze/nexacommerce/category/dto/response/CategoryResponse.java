package com.roze.nexacommerce.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean featured;
    private Boolean active;
    private Long parentId;
    private String parentName;
    private Integer productCount;
    private Integer childrenCount;
    
    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}