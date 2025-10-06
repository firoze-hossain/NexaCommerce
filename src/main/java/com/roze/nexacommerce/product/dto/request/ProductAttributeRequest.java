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
public class ProductAttributeRequest {
    private Long id;
    @NotBlank(message = "Attribute key is required")
    private String name;

    @NotBlank(message = "Attribute value is required")
    private String value;

    private String displayType;

    private Integer displayOrder;
}