package com.roze.nexacommerce.order.dto.request;

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
public class RefundRequest {
    @NotNull(message = "Refund amount is required")
    @PositiveOrZero(message = "Refund amount must be positive or zero")
    private BigDecimal amount;
    
    private String reason;
    
    @Builder.Default
    private Boolean refundToPaymentMethod = true;
}