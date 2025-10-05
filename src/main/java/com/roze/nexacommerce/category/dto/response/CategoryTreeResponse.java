package com.roze.nexacommerce.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private Integer productCount;

    @Builder.Default
    private List<CategoryTreeResponse> children = new ArrayList<>();
}