package com.roze.nexacommerce.order.dto.response;

import com.roze.nexacommerce.order.enums.OrderAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private OrderAction action;
    private String description;
    private String oldValue;
    private String newValue;
    private String notes;
    private Long performedByUserId;
    private String performedByName;
    private LocalDateTime createdAt;
}