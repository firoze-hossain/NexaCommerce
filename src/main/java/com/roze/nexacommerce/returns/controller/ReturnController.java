// ReturnController.java
package com.roze.nexacommerce.returns.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.returns.dto.request.ReturnRequestCreateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnRequestResponse;
import com.roze.nexacommerce.returns.dto.response.ReturnSummaryResponse;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
import com.roze.nexacommerce.returns.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
@Tag(name = "Returns", description = "APIs for managing returns")
public class ReturnController extends BaseController {
    
    private final ReturnService returnService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new return request")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> createReturn(
            @Valid @RequestBody ReturnRequestCreateRequest request,
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user) {
        
        Long customerId = user.getCustomerProfile().getId();
        ReturnRequestResponse response = returnService.createReturnRequest(request, customerId);
        return created(response, "Return request created successfully");
    }
    
    @GetMapping("/{returnId}")
    @PreAuthorize("hasAuthority('VIEW_RETURNS') or @returnSecurityService.canAccessReturn(#returnId, #user)")
    @Operation(summary = "Get return request by ID")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> getReturnById(
            @PathVariable Long returnId,
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user) {
        
        ReturnRequestResponse response = returnService.getReturnById(returnId);
        return ok(response, "Return request retrieved successfully");
    }
    
    @GetMapping("/number/{returnNumber}")
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Get return request by return number")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> getReturnByNumber(@PathVariable String returnNumber) {
        ReturnRequestResponse response = returnService.getReturnByNumber(returnNumber);
        return ok(response, "Return request retrieved successfully");
    }
    
    @GetMapping("/my-returns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer's return requests")
    public ResponseEntity<BaseResponse<List<ReturnRequestResponse>>> getMyReturns(
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user) {
        
        Long customerId = user.getCustomerProfile().getId();
        List<ReturnRequestResponse> response = returnService.getCustomerReturns(customerId);
        return ok(response, "Customer returns retrieved successfully");
    }
    
    @GetMapping("/my-returns/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer's return requests with pagination")
    public ResponseEntity<BaseResponse<PaginatedResponse<ReturnRequestResponse>>> getMyReturnsPaginated(
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Long customerId = user.getCustomerProfile().getId();
        PaginatedResponse<ReturnRequestResponse> response = returnService.getCustomerReturns(customerId, pageable);
        return paginated(response, "Customer returns retrieved successfully");
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Get returns for an order")
    public ResponseEntity<BaseResponse<List<ReturnRequestResponse>>> getOrderReturns(@PathVariable Long orderId) {
        List<ReturnRequestResponse> response = returnService.getOrderReturns(orderId);
        return ok(response, "Order returns retrieved successfully");
    }
    
    @GetMapping("/eligible-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get orders eligible for return")
    public ResponseEntity<BaseResponse<List<ReturnSummaryResponse>>> getEligibleOrders(
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user) {
        
        Long customerId = user.getCustomerProfile().getId();
        List<ReturnSummaryResponse> response = returnService.getEligibleOrders(customerId);
        return ok(response, "Eligible orders retrieved successfully");
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Get returns by status")
    public ResponseEntity<BaseResponse<List<ReturnRequestResponse>>> getReturnsByStatus(@PathVariable ReturnStatus status) {
        List<ReturnRequestResponse> response = returnService.getReturnsByStatus(status);
        return ok(response, "Returns by status retrieved successfully");
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VIEW_RETURNS')")
    @Operation(summary = "Search returns")
    public ResponseEntity<BaseResponse<PaginatedResponse<ReturnRequestResponse>>> searchReturns(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PaginatedResponse<ReturnRequestResponse> response = returnService.searchReturns(query, pageable);
        return paginated(response, "Search results retrieved successfully");
    }
    
    @PatchMapping("/{returnId}/status")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Update return status")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> updateReturnStatus(
            @PathVariable Long returnId,
            @RequestParam ReturnStatus status) {
        
        ReturnRequestResponse response = returnService.updateReturnStatus(returnId, status);
        return ok(response, "Return status updated successfully");
    }
    
    @PatchMapping("/{returnId}/approve")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Approve return request")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> approveReturn(@PathVariable Long returnId) {
        ReturnRequestResponse response = returnService.approveReturn(returnId);
        return ok(response, "Return request approved successfully");
    }
    
    @PatchMapping("/{returnId}/reject")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Reject return request")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> rejectReturn(
            @PathVariable Long returnId,
            @RequestParam String reason) {
        
        ReturnRequestResponse response = returnService.rejectReturn(returnId, reason);
        return ok(response, "Return request rejected successfully");
    }
    
    @PatchMapping("/{returnId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel return request")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> cancelReturn(
            @PathVariable Long returnId,
            @AuthenticationPrincipal com.roze.nexacommerce.user.entity.User user) {
        
        Long customerId = user.getCustomerProfile().getId();
        ReturnRequestResponse response = returnService.cancelReturn(returnId, customerId);
        return ok(response, "Return request cancelled successfully");
    }
    
    @PatchMapping("/{returnId}/process-refund")
    @PreAuthorize("hasAuthority('MANAGE_RETURNS')")
    @Operation(summary = "Process refund for return")
    public ResponseEntity<BaseResponse<ReturnRequestResponse>> processRefund(@PathVariable Long returnId) {
        ReturnRequestResponse response = returnService.processRefund(returnId);
        return ok(response, "Refund processed successfully");
    }
    
    @GetMapping("/reasons")
    @Operation(summary = "Get all return reasons")
    public ResponseEntity<BaseResponse<List<String>>> getReturnReasons() {
        List<String> reasons = List.of(
            "PRODUCT_DEFECTIVE",
            "WRONG_ITEM_SHIPPED", 
            "WRONG_SIZE",
            "NOT_AS_DESCRIBED",
            "CHANGED_MIND",
            "BETTER_PRICE_FOUND",
            "GIFT_RETURN",
            "DELAYED_DELIVERY",
            "MISSING_PARTS",
            "TOO_COMPLICATED",
            "QUALITY_ISSUE",
            "OTHER"
        );
        return ok(reasons, "Return reasons retrieved successfully");
    }
}