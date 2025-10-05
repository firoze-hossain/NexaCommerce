package com.roze.nexacommerce.category.entity;

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
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted = false")
public class Category extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    private String slug;
    
    private String imageUrl;
    
    private Integer displayOrder;
    
    private Boolean featured;
    
    private Boolean active;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    
    // Helper methods
    public void addChildCategory(Category child) {
        children.add(child);
        child.setParent(this);
    }
    
    public void removeChildCategory(Category child) {
        children.remove(child);
        child.setParent(null);
    }
    
    public boolean isRootCategory() {
        return parent == null;
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}