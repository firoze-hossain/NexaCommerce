package com.roze.nexacommerce.review.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verifiedPurchase = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private String title;

    @Builder.Default
    private Integer helpfulCount = 0;

    @Builder.Default
    private Integer notHelpfulCount = 0;

    // Helper methods
    public void markHelpful() {
        this.helpfulCount++;
    }

    public void markNotHelpful() {
        this.notHelpfulCount++;
    }

    public boolean isVerifiedPurchase() {
        return verifiedPurchase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review = (Review) o;
        return getId() != null && getId().equals(review.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}