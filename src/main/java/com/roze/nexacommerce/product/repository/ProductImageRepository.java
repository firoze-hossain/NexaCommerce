package com.roze.nexacommerce.product.repository;

import com.roze.nexacommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);

    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Transactional
    default void safeDeleteByProductId(Long productId) {
        List<ProductImage> images = findByProductId(productId);
        deleteAll(images);
    }
}