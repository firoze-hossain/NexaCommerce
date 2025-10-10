// HotDeal.java
package com.roze.nexacommerce.hotdeal.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.hotdeal.enums.DiscountType;
import com.roze.nexacommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hot_deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDeal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dealPrice;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private Integer stockLimit;

    @Builder.Default
    private Integer soldCount = 0;

    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    @PreUpdate
    private void calculateDealPrice() {
        this.originalPrice = product.getPrice();
        
        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal discountAmount = originalPrice.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            this.dealPrice = originalPrice.subtract(discountAmount).max(BigDecimal.ZERO);
        } else {
            this.dealPrice = originalPrice.subtract(discountValue).max(BigDecimal.ZERO);
        }
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean hasStockAvailable() {
        return stockLimit == null || soldCount < stockLimit;
    }

    public Integer getRemainingStock() {
        return stockLimit != null ? stockLimit - soldCount : null;
    }

    public BigDecimal getDiscountPercentage() {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discountAmount = originalPrice.subtract(dealPrice);
        return discountAmount.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}

