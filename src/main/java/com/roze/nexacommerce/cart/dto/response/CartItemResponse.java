package com.roze.nexacommerce.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private Integer availableStock;
    private Boolean inStock;
    private Boolean lowStock;
}