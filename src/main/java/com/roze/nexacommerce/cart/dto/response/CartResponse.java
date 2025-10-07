package com.roze.nexacommerce.cart.dto.response;

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
public class CartResponse {
    private Long id;
    private Long customerId;
    private Long userId;
    private String sessionId;
    private String cartType;
    private String cartName;
    private Boolean isActive;
    private Boolean isSaved;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer totalUniqueItems;
    private BigDecimal totalAmount;
    private BigDecimal totalDiscount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}