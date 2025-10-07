package com.roze.nexacommerce.brand.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Brand extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(unique = true)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String logoUrl;
    
    private String websiteUrl;
    
    @Builder.Default
    private Boolean featured = false;
    
    @Builder.Default
    private Boolean active = true;
    
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    
    // Helper methods
    public void addProduct(Product product) {
        products.add(product);
        product.setBrand(this);
    }
    
    public void removeProduct(Product product) {
        products.remove(product);
        product.setBrand(null);
    }
    
    public boolean hasProducts() {
        return !products.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand)) return false;
        Brand brand = (Brand) o;
        return getId() != null && getId().equals(brand.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Brand{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}