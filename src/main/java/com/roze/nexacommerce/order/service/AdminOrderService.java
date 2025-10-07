package com.roze.nexacommerce.order.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.order.dto.request.ManualOrderRequest;
import com.roze.nexacommerce.order.dto.request.OrderSearchCriteria;
import com.roze.nexacommerce.order.dto.request.RefundRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.dto.response.OrderStatsResponse;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminOrderService {
    OrderResponse createManualOrder(Long adminUserId, ManualOrderRequest request);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Long adminUserId, String notes);

    OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus, Long adminUserId, String notes);

    OrderResponse processRefund(Long orderId, RefundRequest request, Long adminUserId);

    OrderResponse addOrderNote(Long orderId, String note, Long adminUserId);

    OrderResponse reassignOrder(Long orderId, Long newVendorId, Long adminUserId);

    PaginatedResponse<OrderResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable);

    OrderResponse getOrderWithHistory(Long orderId);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);


    OrderStatsResponse getOrderStats();
}