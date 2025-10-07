package com.roze.nexacommerce.order.dto.request;

import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchCriteria {
    private Long customerId;
    private Long vendorId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
}