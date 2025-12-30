// ReturnSummaryResponse.java
package com.roze.nexacommerce.returns.dto.response;

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
public class ReturnSummaryResponse {
    private Long orderId;
    private String orderNumber;
    private BigDecimal orderTotal;
    private LocalDateTime orderDate;
    private LocalDateTime returnDeadline;
    private Boolean eligibleForReturn;
    private List<ReturnEligibleItemResponse> eligibleItems;
}

