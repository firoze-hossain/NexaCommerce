package com.roze.nexacommerce.order.dto.response;


import com.roze.nexacommerce.order.enums.OrderSource;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long vendorId;
    private String vendorName;
    private Long processedByUserId;
    private String processedByName;
    private OrderSource source;
    private BigDecimal totalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal couponDiscount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private AddressResponse shippingAddress;
    private AddressResponse billingAddress;
    private String customerNotes;
    private String internalNotes;
    private String couponCode;
    private List<OrderItemResponse> items;
    private List<OrderHistoryResponse> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}