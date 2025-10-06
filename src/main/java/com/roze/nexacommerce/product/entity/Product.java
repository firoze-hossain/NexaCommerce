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
import java.util.Iterator;
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
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

    public void clearImages() {
        // Create a copy to avoid ConcurrentModificationException
        List<ProductImage> imagesToRemove = new ArrayList<>(this.images);
        for (ProductImage image : imagesToRemove) {
            removeImage(image);
        }
    }

    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    public void removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
    }

    public void clearAttributes() {
        // Create a copy to avoid ConcurrentModificationException
        List<ProductAttribute> attributesToRemove = new ArrayList<>(this.attributes);
        for (ProductAttribute attribute : attributesToRemove) {
            removeAttribute(attribute);
        }
    }

    // Safe update methods for collections
    public void updateImages(List<ProductImage> newImages) {
        // Remove existing images using iterator to avoid ConcurrentModificationException
        Iterator<ProductImage> iterator = this.images.iterator();
        while (iterator.hasNext()) {
            ProductImage image = iterator.next();
            iterator.remove();
            image.setProduct(null);
        }

        // Add new images
        if (newImages != null) {
            for (ProductImage image : newImages) {
                image.setProduct(this);
                this.images.add(image);
            }
        }
    }

    public void updateAttributes(List<ProductAttribute> newAttributes) {
        // Remove existing attributes using iterator
        Iterator<ProductAttribute> iterator = this.attributes.iterator();
        while (iterator.hasNext()) {
            ProductAttribute attribute = iterator.next();
            iterator.remove();
            attribute.setProduct(null);
        }

        // Add new attributes
        if (newAttributes != null) {
            for (ProductAttribute attribute : newAttributes) {
                attribute.setProduct(this);
                this.attributes.add(attribute);
            }
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return getId() != null && getId().equals(product.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                '}';
    }
}