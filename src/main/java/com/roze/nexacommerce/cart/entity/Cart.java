package com.roze.nexacommerce.cart.entity;

import com.roze.nexacommerce.cart.enums.CartType;
import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CartType type = CartType.CUSTOMER;

    private String cartName;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean isSaved = false;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @Transient
    public BigDecimal getTotalAmount() {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public Integer getTotalItems() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Transient
    public Integer getTotalUniqueItems() {
        return cartItems.size();
    }

    @Transient
    public BigDecimal getTotalDiscount() {
        return cartItems.stream()
                .map(CartItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isCustomerCart() {
        return type == CartType.CUSTOMER && customer != null;
    }

    public boolean isAdminCart() {
        return type == CartType.ADMIN && user != null;
    }

    public boolean isGuestCart() {
        return type == CartType.GUEST && sessionId != null;
    }

    public void addItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public CartItem findItemByProductId(Long productId) {
        return cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public boolean containsProduct(Long productId) {
        return cartItems.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart)) return false;
        Cart cart = (Cart) o;
        return getId() != null && getId().equals(cart.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}