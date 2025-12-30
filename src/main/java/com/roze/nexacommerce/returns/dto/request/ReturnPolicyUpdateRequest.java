// ReturnPolicyUpdateRequest.java
package com.roze.nexacommerce.returns.dto.request;

import com.roze.nexacommerce.returns.enums.ReturnShippingPaidBy;
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
public class ReturnPolicyUpdateRequest {
    private String name;
    private String description;
    
    @PositiveOrZero(message = "Return window days must be positive or zero")
    private Integer returnWindowDays;
    
    @PositiveOrZero(message = "Refund window days must be positive or zero")
    private Integer refundWindowDays;
    
    private BigDecimal freeReturnThreshold;
    private BigDecimal restockingFeePercentage;
    private ReturnShippingPaidBy returnShippingPaidBy;
    private Boolean requireRmaNumber;
    private Boolean allowPartialReturns;
    private Boolean requireOriginalPackaging;
    private String conditionRequirements;
    private Boolean active;
    private Boolean defaultPolicy;
}