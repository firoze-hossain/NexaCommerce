package com.roze.nexacommerce.shipping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingSettingResponse {
    private Long id;
    private String locationType;
    private BigDecimal shippingCost;
    private String deliveryTime;
    private BigDecimal minimumOrderForFreeShipping;
    private boolean active;
    private String description;
    private boolean isFreeShippingEligible;
    private BigDecimal freeShippingRemaining;
}