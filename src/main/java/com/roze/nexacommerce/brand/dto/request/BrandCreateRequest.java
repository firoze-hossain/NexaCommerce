package com.roze.nexacommerce.brand.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCreateRequest {
    
    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    private String slug;
    
    private String description;
    
    private String logoUrl;
    
    private String websiteUrl;
    
    private Boolean featured;
    
    private Boolean active;
}