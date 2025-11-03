package com.roze.nexacommerce.order.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.order.dto.request.GuestOrderCreateRequest;
import com.roze.nexacommerce.order.dto.request.OrderCreateRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    // Customer orders
    OrderResponse createOrder(Long customerId, OrderCreateRequest request);

    OrderResponse getOrderById(Long orderId);

    OrderResponse getOrderByNumber(String orderNumber);

    OrderResponse getOrderForCustomer(Long customerId, Long orderId);

    PaginatedResponse<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable);

    List<OrderResponse> getRecentOrders(Long customerId, int limit);

    OrderResponse cancelOrder(Long orderId, Long customerId);

    // Guest orders
    OrderResponse createGuestOrder(GuestOrderCreateRequest request);

    OrderResponse getGuestOrder(String orderNumber, String email);

    // Admin/Vendor operations
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

    OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus);

    OrderResponse addOrderNote(Long orderId, String note);

    PaginatedResponse<OrderResponse> getAllOrders(Pageable pageable);
}