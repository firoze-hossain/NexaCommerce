// HotDealResponse.java
package com.roze.nexacommerce.hotdeal.dto.response;


import com.roze.nexacommerce.hotdeal.enums.DiscountType;
import com.roze.nexacommerce.product.dto.response.ProductResponse;
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
public class HotDealResponse {
    private Long id;
    private ProductResponse product;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal originalPrice;
    private BigDecimal dealPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer stockLimit;
    private Integer soldCount;
    private Integer remainingStock;
    private Boolean isActive;
    private Boolean isCurrentlyActive;
    private BigDecimal discountPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}