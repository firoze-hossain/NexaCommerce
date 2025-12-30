// ReturnServiceImpl.java
package com.roze.nexacommerce.returns.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.repository.OrderRepository;
import com.roze.nexacommerce.returns.dto.request.ReturnRequestCreateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnRequestResponse;
import com.roze.nexacommerce.returns.dto.response.ReturnSummaryResponse;
import com.roze.nexacommerce.returns.entity.ReturnItem;
import com.roze.nexacommerce.returns.entity.ReturnPolicy;
import com.roze.nexacommerce.returns.entity.ReturnRequest;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
import com.roze.nexacommerce.returns.mapper.ReturnMapper;
import com.roze.nexacommerce.returns.repository.ReturnPolicyRepository;
import com.roze.nexacommerce.returns.repository.ReturnRequestRepository;
import com.roze.nexacommerce.returns.service.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnServiceImpl implements ReturnService {
    
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnPolicyRepository returnPolicyRepository;
    private final OrderRepository orderRepository;
    private final ReturnMapper returnMapper;
    
    @Override
    @Transactional
    public ReturnRequestResponse createReturnRequest(ReturnRequestCreateRequest request, Long customerId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));
        
        // Validate order belongs to customer
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }
        
        // Check if order is eligible for return
        if (!isOrderEligibleForReturn(order)) {
            throw new IllegalStateException("Order is not eligible for return");
        }
        
        // Get default return policy
        ReturnPolicy policy = returnPolicyRepository.findDefaultPolicy()
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "default", "true"));
        
        // Create return request
        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnNumber(generateReturnNumber())
                .order(order)
                .customer(order.getCustomer())
                .reason(request.getReason())
                .reasonDescription(request.getReasonDescription())
                .status(ReturnStatus.REQUESTED)
                .refundMethod(request.getRefundMethod() != null ? request.getRefundMethod() : "original_payment")
                .notes(request.getNotes())
                .build();
        
        // Add return items
        for (Long itemId : request.getItemIds()) {
            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Order Item", "id", itemId));
            
            // Validate item can be returned
            if (!isOrderItemReturnable(orderItem)) {
                throw new IllegalStateException("Item " + orderItem.getProductName() + " is not returnable");
            }
            
            ReturnItem returnItem = ReturnItem.builder()
                    .returnRequest(returnRequest)
                    .orderItem(orderItem)
                    .quantity(orderItem.getQuantity())
                    .refundAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                    .conditionReceived("NEW") // Default assumption
                    .build();
            
            returnRequest.getReturnItems().add(returnItem);
        }
        
        // Calculate refund amounts
        returnRequest.calculateRefund();
        
        // Apply return policy
        applyReturnPolicy(returnRequest, policy);
        
        ReturnRequest savedReturn = returnRequestRepository.save(returnRequest);
        
        log.info("Created return request: {} for order: {}", 
                savedReturn.getReturnNumber(), order.getOrderNumber());
        
        return returnMapper.toResponse(savedReturn);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getReturnById(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        return returnMapper.toResponse(returnRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getReturnByNumber(String returnNumber) {
        ReturnRequest returnRequest = returnRequestRepository.findByReturnNumber(returnNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "number", returnNumber));
        return returnMapper.toResponse(returnRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getCustomerReturns(Long customerId) {
        List<ReturnRequest> returns = returnRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return returns.stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestResponse> getCustomerReturns(Long customerId, Pageable pageable) {
        Page<ReturnRequest> returnPage = returnRequestRepository.findByCustomerId(customerId, pageable);
        
        List<ReturnRequestResponse> returnResponses = returnPage.getContent().stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
        
        return PaginatedResponse.<ReturnRequestResponse>builder()
                .items(returnResponses)
                .totalItems(returnPage.getTotalElements())
                .currentPage(returnPage.getNumber())
                .pageSize(returnPage.getSize())
                .totalPages(returnPage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getOrderReturns(Long orderId) {
        List<ReturnRequest> returns = returnRequestRepository.findByOrderId(orderId);
        return returns.stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnSummaryResponse> getEligibleOrders(Long customerId) {
        // Convert OrderStatus enums to strings
        List<String> eligibleStatuses = Arrays.asList(
                OrderStatus.DELIVERED.name(),
                OrderStatus.SHIPPED.name()
        );

        List<Order> orders = orderRepository.findByCustomerIdAndStatusIn(
                customerId,
                eligibleStatuses // Pass as List<String>
        );

        return orders.stream()
                .filter(this::isOrderEligibleForReturn)
                .map(returnMapper::toReturnSummary)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ReturnRequestResponse updateReturnStatus(Long returnId, ReturnStatus status) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        
        returnRequest.setStatus(status);
        ReturnRequest updatedReturn = returnRequestRepository.save(returnRequest);
        
        log.info("Updated return {} status to: {}", 
                updatedReturn.getReturnNumber(), status);
        
        return returnMapper.toResponse(updatedReturn);
    }
    
    @Override
    @Transactional
    public ReturnRequestResponse approveReturn(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        
        // Generate return label
        returnRequest.setReturnLabelUrl(generateReturnLabel(returnRequest));
        returnRequest.setCarrier("UPS");
        returnRequest.setTrackingNumber(generateTrackingNumber());
        returnRequest.setStatus(ReturnStatus.APPROVED);
        
        ReturnRequest updatedReturn = returnRequestRepository.save(returnRequest);
        
        log.info("Approved return: {}", updatedReturn.getReturnNumber());
        
        return returnMapper.toResponse(updatedReturn);
    }
    
    @Override
    @Transactional
    public ReturnRequestResponse rejectReturn(Long returnId, String reason) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        
        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setNotes(returnRequest.getNotes() + "\nRejection reason: " + reason);
        
        ReturnRequest updatedReturn = returnRequestRepository.save(returnRequest);
        
        log.info("Rejected return: {} - Reason: {}", 
                updatedReturn.getReturnNumber(), reason);
        
        return returnMapper.toResponse(updatedReturn);
    }
    
    @Override
    @Transactional
    public ReturnRequestResponse cancelReturn(Long returnId, Long customerId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        
        // Validate customer owns the return
        if (!returnRequest.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Return does not belong to customer");
        }
        
        // Only allow cancellation if in certain statuses
        if (!List.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED).contains(returnRequest.getStatus())) {
            throw new IllegalStateException("Cannot cancel return in current status: " + returnRequest.getStatus());
        }
        
        returnRequest.setStatus(ReturnStatus.CANCELLED);
        ReturnRequest updatedReturn = returnRequestRepository.save(returnRequest);
        
        log.info("Cancelled return: {}", updatedReturn.getReturnNumber());
        
        return returnMapper.toResponse(updatedReturn);
    }
    
    @Override
    @Transactional
    public ReturnRequestResponse processRefund(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request", "id", returnId));
        
        // Process refund through payment gateway (simplified)
        boolean refundSuccess = processPaymentRefund(returnRequest);
        
        if (refundSuccess) {
            returnRequest.setStatus(ReturnStatus.REFUNDED);
            returnRequest.setNotes(returnRequest.getNotes() + "\nRefund processed on: " + LocalDateTime.now());
            
            log.info("Processed refund for return: {} - Amount: {}", 
                    returnRequest.getReturnNumber(), returnRequest.getRefundAmount());
        } else {
            returnRequest.setStatus(ReturnStatus.REFUND_PROCESSING);
            log.warn("Refund processing failed for return: {}", returnRequest.getReturnNumber());
        }
        
        ReturnRequest updatedReturn = returnRequestRepository.save(returnRequest);
        
        return returnMapper.toResponse(updatedReturn);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getReturnsByStatus(ReturnStatus status) {
        List<ReturnRequest> returns = returnRequestRepository.findByStatus(status);
        return returns.stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestResponse> searchReturns(String query, Pageable pageable) {
        Page<ReturnRequest> returnPage = returnRequestRepository.searchReturns(query, pageable);
        
        List<ReturnRequestResponse> returnResponses = returnPage.getContent().stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
        
        return PaginatedResponse.<ReturnRequestResponse>builder()
                .items(returnResponses)
                .totalItems(returnPage.getTotalElements())
                .currentPage(returnPage.getNumber())
                .pageSize(returnPage.getSize())
                .totalPages(returnPage.getTotalPages())
                .build();
    }
    
    // Helper Methods
    private boolean isOrderEligibleForReturn(Order order) {
        // Check if order is within return window (30 days)
        LocalDateTime returnDeadline = order.getCreatedAt().plusDays(30);
        if (LocalDateTime.now().isAfter(returnDeadline)) {
            return false;
        }
        
        // Check order status - only delivered/completed orders can be returned
        OrderStatus status = order.getStatus();
        return status == OrderStatus.DELIVERED || status == OrderStatus.SHIPPED;
    }
    
    private boolean isOrderItemReturnable(OrderItem orderItem) {
        return isOrderEligibleForReturn(orderItem.getOrder());
    }
    
    private void applyReturnPolicy(ReturnRequest returnRequest, ReturnPolicy policy) {
        // Apply restocking fee
        if (policy.getRestockingFeePercentage() != null && 
            policy.getRestockingFeePercentage().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal restockingFee = returnRequest.getTotalAmount()
                    .multiply(policy.getRestockingFeePercentage())
                    .divide(BigDecimal.valueOf(100));
            
            returnRequest.setRestockingFee(restockingFee);
        }
        
        // Apply return shipping cost
        if (policy.getReturnShippingPaidBy() != null) {
            switch (policy.getReturnShippingPaidBy()) {
                case BUYER:
                    returnRequest.setReturnShippingCost(BigDecimal.valueOf(9.99)); // Example flat rate
                    break;
                case CONDITIONAL:
                    // Check if order meets free return threshold
                    if (policy.getFreeReturnThreshold() != null &&
                        returnRequest.getOrder().getFinalAmount().compareTo(policy.getFreeReturnThreshold()) < 0) {
                        returnRequest.setReturnShippingCost(BigDecimal.valueOf(9.99));
                    }
                    break;
                case SELLER:
                default:
                    returnRequest.setReturnShippingCost(BigDecimal.ZERO);
                    break;
            }
        }
        
        // Recalculate refund after applying policy
        returnRequest.calculateRefund();
    }
    
    private String generateReturnNumber() {
        return "RMA-" + LocalDateTime.now().getYear() + "-" +
               String.format("%06d", returnRequestRepository.count() + 1);
    }
    
    private String generateTrackingNumber() {
        return "1Z" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
    }
    
    private String generateReturnLabel(ReturnRequest returnRequest) {
        // In production, integrate with shipping carrier API
        return "https://api.example.com/return-labels/" + returnRequest.getReturnNumber() + ".pdf";
    }
    
    private boolean processPaymentRefund(ReturnRequest returnRequest) {
        // Integrate with payment gateway (Stripe, PayPal, etc.)
        // This is a simplified version - in production, call actual payment gateway API
        log.info("Processing refund of {} for return {} via method: {}", 
                returnRequest.getRefundAmount(), 
                returnRequest.getReturnNumber(), 
                returnRequest.getRefundMethod());
        
        // Simulate successful refund
        return true;
    }
}