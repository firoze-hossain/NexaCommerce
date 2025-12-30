// ReturnRequestCreateRequest.java
package com.roze.nexacommerce.returns.dto.request;

import com.roze.nexacommerce.returns.enums.ReturnReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestCreateRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Return reason is required")
    private ReturnReason reason;
    
    private String reasonDescription;
    
    @NotNull(message = "Item IDs are required")
    private List<Long> itemIds;
    
    private String refundMethod; // original_payment, store_credit, gift_card
    
    private String notes;
}