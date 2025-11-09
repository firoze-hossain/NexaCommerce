package com.roze.nexacommerce.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String guestEmail;

    @NotBlank(message = "Guest name is required")
    private String guestName;

//    @NotBlank(message = "Phone is required")
//    private String guestPhone;

    @Valid
    @NotNull(message = "Shipping address is required")
    private GuestAddressRequest shippingAddress;

    private String customerNotes;

    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String couponCode;

    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;

    @Builder.Default
    private Boolean sendEmailReceipt = true;
}