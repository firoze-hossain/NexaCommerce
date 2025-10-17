package com.roze.nexacommerce.order.repository;

import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.enums.OrderSource;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomer(CustomerProfile customer);
    Page<Order> findByCustomer(CustomerProfile customer, Pageable pageable);
    List<Order> findByVendor(VendorProfile vendor);
    Page<Order> findByVendor(VendorProfile vendor, Pageable pageable);
    List<Order> findByProcessedBy(User user);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Order> findByStatusAndPaymentStatus(OrderStatus status, PaymentStatus paymentStatus);
    List<Order> findBySource(OrderSource source);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.status IN :statuses")
    Long countByCustomerAndStatusIn(@Param("customer") CustomerProfile customer, 
                                   @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.vendor = :vendor AND o.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenueByVendor(@Param("vendor") VendorProfile vendor);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer")
    Long countByCustomer(@Param("customer") CustomerProfile customer);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:customerId IS NULL OR o.customer.id = :customerId) AND " +
           "(:vendorId IS NULL OR o.vendor.id = :vendorId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:source IS NULL OR o.source = :source) AND " +
           "(o.orderNumber LIKE %:searchTerm% OR o.customer.user.name LIKE %:searchTerm%)")
    Page<Order> searchOrders(@Param("customerId") Long customerId,
                            @Param("vendorId") Long vendorId,
                            @Param("status") OrderStatus status,
                            @Param("paymentStatus") PaymentStatus paymentStatus,
                            @Param("source") OrderSource source,
                            @Param("searchTerm") String searchTerm,
                            Pageable pageable);
    
    boolean existsByOrderNumber(String orderNumber);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID'")
    BigDecimal getRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    List<Order> findByCustomerIdAndStatusIn(Long customerId, List<String> delivered);
}