// ReturnPolicyResponse.java
package com.roze.nexacommerce.returns.dto.response;

import com.roze.nexacommerce.returns.enums.ReturnShippingPaidBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnPolicyResponse {
    private Long id;
    private String name;
    private String description;
    private Integer returnWindowDays;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}