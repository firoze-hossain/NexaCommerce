// ReturnItemRepository.java
package com.roze.nexacommerce.returns.repository;

import com.roze.nexacommerce.returns.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    
    List<ReturnItem> findByReturnRequestId(Long returnRequestId);
    
    @Query("SELECT ri FROM ReturnItem ri WHERE ri.orderItem.product.id = :productId AND ri.returnRequest.status = 'COMPLETED'")
    List<ReturnItem> findCompletedReturnsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT SUM(ri.quantity) FROM ReturnItem ri WHERE ri.orderItem.product.id = :productId")
    Integer countReturnedQuantityByProductId(@Param("productId") Long productId);
}