// ReturnRequestRepository.java
package com.roze.nexacommerce.returns.repository;

import com.roze.nexacommerce.returns.entity.ReturnRequest;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    
    Optional<ReturnRequest> findByReturnNumber(String returnNumber);
    
    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    List<ReturnRequest> findByOrderId(Long orderId);
    
    Page<ReturnRequest> findByCustomerId(Long customerId, Pageable pageable);
    
    List<ReturnRequest> findByStatus(ReturnStatus status);
    
    @Query("SELECT r FROM ReturnRequest r WHERE r.status = :status AND r.createdAt >= :startDate")
    List<ReturnRequest> findByStatusAndCreatedAfter(
            @Param("status") ReturnStatus status,
            @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(r) FROM ReturnRequest r WHERE r.customer.id = :customerId AND r.status = 'COMPLETED'")
    Long countCompletedReturnsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT r FROM ReturnRequest r WHERE r.order.id = :orderId AND r.status NOT IN ('CANCELLED', 'REJECTED')")
    List<ReturnRequest> findActiveReturnsByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT r FROM ReturnRequest r WHERE " +
           "r.returnNumber LIKE %:query% OR " +
           "r.order.orderNumber LIKE %:query% OR " +
           "r.customer.user.name LIKE %:query% OR " +
           "r.customer.user.email LIKE %:query%")
    Page<ReturnRequest> searchReturns(@Param("query") String query, Pageable pageable);
}