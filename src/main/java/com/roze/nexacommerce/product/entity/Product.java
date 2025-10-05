package com.roze.nexacommerce.product.entity;

import com.roze.nexacommerce.category.entity.Category;
import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.product.enums.ProductStatus;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
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
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Product extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorProfile vendor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String shortDescription;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal compareAtPrice;
    
    @Column(nullable = false)
    private Integer stock;
    
    private Integer lowStockThreshold;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    private String barcode;
    
    private Boolean trackQuantity;
    
    private Boolean allowBackorder;
    
    private BigDecimal weight;
    
    private String weightUnit;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    
    private Boolean featured;
    
    private Boolean published;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String tags;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();
    
    // Helper methods
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }
    
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
    
    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }
    
    public void removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
    }
    
    public boolean isInStock() {
        return stock > 0;
    }
    
    public boolean isLowStock() {
        return lowStockThreshold != null && stock <= lowStockThreshold;
    }
    
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && published && (stock > 0 || allowBackorder);
    }
}