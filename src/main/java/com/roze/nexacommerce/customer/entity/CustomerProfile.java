package com.roze.nexacommerce.customer.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.customer.enums.CustomerStatus;
import com.roze.nexacommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String phone;

    private String profileImage;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_spent", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    // Preferences
    private String currency;
    private String language;
    private Boolean newsletterSubscribed = false;

    // Stats
    @Builder.Default
    private Integer wishlistCount = 0;

    @Builder.Default
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    public void addOrderAmount(BigDecimal amount) {
        this.totalOrders++;
        this.totalSpent = this.totalSpent.add(amount);
        calculateLoyaltyPoints();
    }

    private void calculateLoyaltyPoints() {
        this.loyaltyPoints = this.totalSpent.divide(BigDecimal.TEN).intValue(); // 1 point per $10
    }

    public void incrementWishlistCount() {
        this.wishlistCount++;
    }

    public void decrementWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }
}