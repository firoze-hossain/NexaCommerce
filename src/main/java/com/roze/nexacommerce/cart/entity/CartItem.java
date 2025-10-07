package com.roze.nexacommerce.cart.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

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

    public void incrementQuantity(Integer amount) {
        this.quantity += amount;
    }

    public void decrementQuantity(Integer amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }

    public void updateQuantity(Integer newQuantity) {
        this.quantity = Math.max(0, newQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return getId() != null && getId().equals(cartItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}