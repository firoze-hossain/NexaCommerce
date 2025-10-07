package com.roze.nexacommerce.brand.dto.response;

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
public class BrandWithProductsResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private Boolean featured;
    private Boolean active;

    @Builder.Default
    private List<ProductSummaryResponse> products = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}