package com.roze.nexacommerce.customer.dto.response;

import com.roze.nexacommerce.customer.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String phone;
    private String profileImage;
    private LocalDateTime dateOfBirth;
    private Integer loyaltyPoints;
    private Integer totalOrders;
    private Double totalSpent;
    private String currency;
    private String language;
    private Boolean newsletterSubscribed;
    private Integer wishlistCount;
    private Integer reviewCount;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

