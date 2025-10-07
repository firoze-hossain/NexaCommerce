package com.roze.nexacommerce.order.repository;

import com.roze.nexacommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.vendor.id = :vendorId")
    List<OrderItem> findByVendorId(@Param("vendorId") Long vendorId);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalSoldQuantityByProductId(@Param("productId") Long productId);
    
    @Query("SELECT oi.product.id, SUM(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.order.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product.id ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
}