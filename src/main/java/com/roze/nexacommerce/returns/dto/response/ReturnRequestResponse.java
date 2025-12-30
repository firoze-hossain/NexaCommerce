// ReturnRequestResponse.java
package com.roze.nexacommerce.returns.dto.response;

import com.roze.nexacommerce.returns.enums.ReturnReason;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
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
public class ReturnRequestResponse {
    private Long id;
    private String returnNumber;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private ReturnReason reason;
    private String reasonDescription;
    private ReturnStatus status;
    private BigDecimal totalAmount;
    private BigDecimal refundAmount;
    private BigDecimal restockingFee;
    private BigDecimal returnShippingCost;
    private Boolean pickupRequired;
    private LocalDateTime pickupScheduledAt;
    private String returnLabelUrl;
    private String trackingNumber;
    private String carrier;
    private String refundMethod;
    private String notes;
    private List<ReturnItemResponse> returnItems;
    private Boolean eligibleForFreeReturn;
    private Boolean withinReturnWindow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}