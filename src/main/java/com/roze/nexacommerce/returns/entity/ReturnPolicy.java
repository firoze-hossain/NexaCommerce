// ReturnPolicy.java
package com.roze.nexacommerce.returns.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.returns.enums.ReturnShippingPaidBy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "return_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnPolicy extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "return_window_days", nullable = false)
    @Builder.Default
    private Integer returnWindowDays = 30;
    
    @Column(name = "refund_window_days", nullable = false)
    @Builder.Default
    private Integer refundWindowDays = 7;
    
    @Column(name = "free_return_threshold", precision = 10, scale = 2)
    private BigDecimal freeReturnThreshold;
    
    @Column(name = "restocking_fee_percentage")
    @Builder.Default
    private BigDecimal restockingFeePercentage = BigDecimal.ZERO;
    
    @Column(name = "return_shipping_paid_by")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnShippingPaidBy returnShippingPaidBy = ReturnShippingPaidBy.SELLER;
    
    @Column(name = "require_rma_number")
    @Builder.Default
    private Boolean requireRmaNumber = false;
    
    @Column(name = "allow_partial_returns")
    @Builder.Default
    private Boolean allowPartialReturns = true;
    
    @Column(name = "require_original_packaging")
    @Builder.Default
    private Boolean requireOriginalPackaging = true;
    
    @Column(name = "condition_requirements", columnDefinition = "TEXT")
    private String conditionRequirements;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean defaultPolicy = false;
}

