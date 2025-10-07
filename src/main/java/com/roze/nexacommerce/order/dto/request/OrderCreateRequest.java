package com.roze.nexacommerce.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private Long billingAddressId;

    @Builder.Default
    private Boolean useShippingAsBilling = true;

    private String customerNotes;

    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String couponCode;

    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;

    // Add these methods for clarity
    public boolean shouldUseShippingAsBilling() {
        return Boolean.TRUE.equals(useShippingAsBilling) || billingAddressId == null;
    }
}