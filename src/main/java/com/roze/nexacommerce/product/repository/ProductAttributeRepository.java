package com.roze.nexacommerce.product.repository;

import com.roze.nexacommerce.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    List<ProductAttribute> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM ProductAttribute pa WHERE pa.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Transactional
    default void safeDeleteByProductId(Long productId) {
        List<ProductAttribute> attributes = findByProductId(productId);
        deleteAll(attributes);
    }

}