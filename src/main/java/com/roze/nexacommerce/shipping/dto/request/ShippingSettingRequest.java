package com.roze.nexacommerce.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingSettingRequest {

    @NotBlank(message = "Location type is required")
    private String locationType;

    @NotNull(message = "Shipping cost is required")
    @PositiveOrZero(message = "Shipping cost must be positive or zero")
    private BigDecimal shippingCost;

    @NotBlank(message = "Delivery time is required")
    private String deliveryTime;

    @NotNull(message = "Minimum order for free shipping is required")
    @PositiveOrZero(message = "Minimum order must be positive or zero")
    private BigDecimal minimumOrderForFreeShipping;

    @Builder.Default
    private boolean active = true;

    private String description;
}