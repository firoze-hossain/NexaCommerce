package com.roze.nexacommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String altText;
    
    private Integer displayOrder;
    
    private Boolean isPrimary;
}