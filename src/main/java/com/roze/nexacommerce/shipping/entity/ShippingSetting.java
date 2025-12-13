package com.roze.nexacommerce.shipping.entity;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class ShippingSetting extends BaseEntity {

    @Column(name = "location_type", nullable = false, unique = true)
    private String locationType; // INSIDE_DHAKA, OUTSIDE_DHAKA

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "delivery_time", nullable = false)
    private String deliveryTime; // e.g., "1-2 days", "3-5 days"

    @Column(name = "minimum_order_for_free_shipping", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderForFreeShipping = new BigDecimal("1000.00");

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "description")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingSetting)) return false;
        ShippingSetting that = (ShippingSetting) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShippingSetting{" +
                "id=" + getId() +
                ", locationType='" + locationType + '\'' +
                ", shippingCost=" + shippingCost +
                '}';
    }
}