// HotDealUpdateRequest.java
package com.roze.nexacommerce.hotdeal.dto.request;

import com.roze.nexacommerce.hotdeal.enums.DiscountType;
import jakarta.validation.constraints.Positive;
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
public class HotDealUpdateRequest {
    
    private DiscountType discountType;
    
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer stockLimit;
    
    private Boolean isActive;
}