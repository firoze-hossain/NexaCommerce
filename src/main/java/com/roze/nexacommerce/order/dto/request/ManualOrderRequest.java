package com.roze.nexacommerce.order.dto.request;

import com.roze.nexacommerce.order.enums.OrderSource;
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
public class ManualOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    private Long billingAddressId;
    
    @Builder.Default
    private Boolean useShippingAsBilling = true;
    
    private String customerNotes;
    private String internalNotes;
    
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    private String couponCode;
    
    @Builder.Default
    private OrderSource source = OrderSource.ADMIN_PANEL;
    
    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;
}