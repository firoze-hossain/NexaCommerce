package com.roze.nexacommerce.product.repository;

import com.roze.nexacommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);
    
    List<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
    
    void deleteByProductId(Long productId);
}