package com.roze.nexacommerce.vendor.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.vendor.enums.BusinessType;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Entity
@Table(name = "vendors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfile extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true)
    private String businessEmail;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private String taxNumber;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String website;

    // Business details
    private String businessRegistrationNumber;
    private String bankAccountNumber;
    private String bankName;

    // Platform settings
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("5.0");

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VendorStatus status = VendorStatus.PENDING;

    // Stats
    @Builder.Default
    private Integer totalProducts = 0;

    @Builder.Default
    private Integer totalOrders = 0;

    @Builder.Default
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "rating_avg", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Builder.Default
    private Integer ratingCount = 0;



    // Helper methods
    public void addProduct() {
        this.totalProducts++;
    }

    public void removeProduct() {
        if (this.totalProducts > 0) {
            this.totalProducts--;
        }
    }

    public void addSale(BigDecimal amount) {
        this.totalOrders++;
        this.totalSales = this.totalSales.add(amount);
    }

    public void updateRating(BigDecimal newRating) {
        BigDecimal totalRating = this.ratingAvg.multiply(BigDecimal.valueOf(this.ratingCount)).add(newRating);
        this.ratingCount++;
        this.ratingAvg = totalRating.divide(BigDecimal.valueOf(this.ratingCount), 2, RoundingMode.HALF_UP);
    }

    public boolean isActive() {
        return this.status == VendorStatus.APPROVED && this.user.getActive();
    }
}