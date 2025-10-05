package com.roze.nexacommerce.category.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Long parentId;
    private Integer displayOrder;
    private Boolean featured;
    private Boolean active;
}