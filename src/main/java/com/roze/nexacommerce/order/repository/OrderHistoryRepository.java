package com.roze.nexacommerce.order.repository;

import com.roze.nexacommerce.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<OrderHistory> findByOrderIdAndAction(Long orderId, com.roze.nexacommerce.order.enums.OrderAction action);
}