package com.roze.nexacommerce.product.entity;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_attributes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    private String displayType; // TEXT, COLOR, IMAGE, etc.

    private Integer displayOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductAttribute)) return false;
        ProductAttribute that = (ProductAttribute) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductAttribute{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}