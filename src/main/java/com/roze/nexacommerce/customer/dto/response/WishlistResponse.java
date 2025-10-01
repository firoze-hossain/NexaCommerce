package com.roze.nexacommerce.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double productPrice;
    private String productImage;
    private String notes;
    private LocalDateTime addedAt;
}