// ReturnPolicyCreateRequest.java
package com.roze.nexacommerce.returns.dto.request;

import com.roze.nexacommerce.returns.enums.ReturnShippingPaidBy;
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
public class ReturnPolicyCreateRequest {
    
    @NotBlank(message = "Policy name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Return window days is required")
    @PositiveOrZero(message = "Return window days must be positive or zero")
    @Builder.Default
    private Integer returnWindowDays = 30;
    
    @NotNull(message = "Refund window days is required")
    @PositiveOrZero(message = "Refund window days must be positive or zero")
    @Builder.Default
    private Integer refundWindowDays = 7;
    
    private BigDecimal freeReturnThreshold;
    
    @Builder.Default
    private BigDecimal restockingFeePercentage = BigDecimal.ZERO;
    
    @NotNull(message = "Return shipping paid by is required")
    @Builder.Default
    private ReturnShippingPaidBy returnShippingPaidBy = ReturnShippingPaidBy.SELLER;
    
    @Builder.Default
    private Boolean requireRmaNumber = false;
    
    @Builder.Default
    private Boolean allowPartialReturns = true;
    
    @Builder.Default
    private Boolean requireOriginalPackaging = true;
    
    private String conditionRequirements;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean defaultPolicy = false;
}