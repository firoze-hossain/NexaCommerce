// ReturnService.java
package com.roze.nexacommerce.returns.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.returns.dto.request.ReturnRequestCreateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnRequestResponse;
import com.roze.nexacommerce.returns.dto.response.ReturnSummaryResponse;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReturnService {
    ReturnRequestResponse createReturnRequest(ReturnRequestCreateRequest request, Long customerId);
    ReturnRequestResponse getReturnById(Long returnId);
    ReturnRequestResponse getReturnByNumber(String returnNumber);
    List<ReturnRequestResponse> getCustomerReturns(Long customerId);
    PaginatedResponse<ReturnRequestResponse> getCustomerReturns(Long customerId, Pageable pageable);
    List<ReturnRequestResponse> getOrderReturns(Long orderId);
    List<ReturnSummaryResponse> getEligibleOrders(Long customerId);
    ReturnRequestResponse updateReturnStatus(Long returnId, ReturnStatus status);
    ReturnRequestResponse approveReturn(Long returnId);
    ReturnRequestResponse rejectReturn(Long returnId, String reason);
    ReturnRequestResponse cancelReturn(Long returnId, Long customerId);
    ReturnRequestResponse processRefund(Long returnId);
    List<ReturnRequestResponse> getReturnsByStatus(ReturnStatus status);
    PaginatedResponse<ReturnRequestResponse> searchReturns(String query, Pageable pageable);
}