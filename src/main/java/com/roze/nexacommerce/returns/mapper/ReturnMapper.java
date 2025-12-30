// ReturnMapper.java
package com.roze.nexacommerce.returns.mapper;

import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyCreateRequest;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyUpdateRequest;
import com.roze.nexacommerce.returns.dto.response.*;
import com.roze.nexacommerce.returns.entity.ReturnItem;
import com.roze.nexacommerce.returns.entity.ReturnPolicy;
import com.roze.nexacommerce.returns.entity.ReturnRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReturnMapper {
    private final ModelMapper modelMapper;

    // ReturnPolicy Mappings
    public ReturnPolicy toEntity(ReturnPolicyCreateRequest request) {
        return modelMapper.map(request, ReturnPolicy.class);
    }

    public ReturnPolicyResponse toResponse(ReturnPolicy policy) {
        return modelMapper.map(policy, ReturnPolicyResponse.class);
    }

    public void updateEntity(ReturnPolicyUpdateRequest request, ReturnPolicy policy) {
        modelMapper.map(request, policy);
    }

    // ReturnRequest Mappings
    public ReturnRequestResponse toResponse(ReturnRequest returnRequest) {
        ReturnRequestResponse response = modelMapper.map(returnRequest, ReturnRequestResponse.class);
        
        // Set additional fields
        if (returnRequest.getOrder() != null) {
            response.setOrderId(returnRequest.getOrder().getId());
            response.setOrderNumber(returnRequest.getOrder().getOrderNumber());
        }
        
        if (returnRequest.getCustomer() != null) {
            response.setCustomerId(returnRequest.getCustomer().getId());
            if (returnRequest.getCustomer().getUser() != null) {
                response.setCustomerName(returnRequest.getCustomer().getUser().getName());
            }
        }
        
        // Set helper fields
        response.setEligibleForFreeReturn(returnRequest.isEligibleForFreeReturn());
        response.setWithinReturnWindow(returnRequest.isWithinReturnWindow());
        
        // Map return items
        if (returnRequest.getReturnItems() != null) {
            List<ReturnItemResponse> itemResponses = returnRequest.getReturnItems().stream()
                    .map(this::toReturnItemResponse)
                    .collect(Collectors.toList());
            response.setReturnItems(itemResponses);
        }
        
        return response;
    }

    public ReturnItemResponse toReturnItemResponse(ReturnItem returnItem) {
        ReturnItemResponse response = modelMapper.map(returnItem, ReturnItemResponse.class);
        
        if (returnItem.getOrderItem() != null) {
            response.setOrderItemId(returnItem.getOrderItem().getId());
            
            if (returnItem.getOrderItem().getProduct() != null) {
                response.setProductId(returnItem.getOrderItem().getProduct().getId());
                response.setProductName(returnItem.getOrderItem().getProductName());
                response.setProductImage(returnItem.getOrderItem().getProductImage());
            }
        }
        
        return response;
    }

    // Return Summary Mappings
    public ReturnSummaryResponse toReturnSummary(Order order) {
        ReturnSummaryResponse response = new ReturnSummaryResponse();
        
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOrderTotal(order.getFinalAmount());
        response.setOrderDate(order.getCreatedAt());
        response.setReturnDeadline(order.getCreatedAt().plusDays(30));
        response.setEligibleForReturn(isOrderEligibleForReturn(order));
        
        // Map eligible items
        if (order.getOrderItems() != null) {
            List<ReturnEligibleItemResponse> eligibleItems = order.getOrderItems().stream()
                    .map(this::toReturnEligibleItemResponse)
                    .collect(Collectors.toList());
            response.setEligibleItems(eligibleItems);
        }
        
        return response;
    }

    public ReturnEligibleItemResponse toReturnEligibleItemResponse(OrderItem orderItem) {
        ReturnEligibleItemResponse response = new ReturnEligibleItemResponse();
        
        response.setOrderItemId(orderItem.getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setProductName(orderItem.getProductName());
        response.setProductImage(orderItem.getProductImage());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());
        response.setReturnable(isOrderItemReturnable(orderItem));
        response.setReturnableReason(getReturnableReason(orderItem));
        
        return response;
    }

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
        // Basic check - item must have been delivered
        Order order = orderItem.getOrder();
        return isOrderEligibleForReturn(order);
    }

    private String getReturnableReason(OrderItem orderItem) {
        if (!isOrderItemReturnable(orderItem)) {
            Order order = orderItem.getOrder();
            LocalDateTime returnDeadline = order.getCreatedAt().plusDays(30);
            
            if (LocalDateTime.now().isAfter(returnDeadline)) {
                return "Return window expired";
            }
            
            return "Order not eligible for return";
        }
        
        return "Eligible for return";
    }
}