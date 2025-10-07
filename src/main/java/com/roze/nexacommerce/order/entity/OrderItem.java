package com.roze.nexacommerce.order.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    private String productName;
    private String productSku;
    private String productImage;

    @Transient
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Transient
    public BigDecimal getDiscountAmount() {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) > 0) {
            return compareAtPrice.subtract(price).multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    private void setProductDetails() {
        if (product != null) {
            if (productName == null) {
                productName = product.getName();
            }
            if (productSku == null) {
                productSku = product.getSku();
            }
            if (productImage == null && !product.getImages().isEmpty()) {
                productImage = product.getImages().get(0).getImageUrl();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem orderItem = (OrderItem) o;
        return getId() != null && getId().equals(orderItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}