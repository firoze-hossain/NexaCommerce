package com.roze.nexacommerce.order.mapper;

import com.roze.nexacommerce.order.dto.response.OrderHistoryResponse;
import com.roze.nexacommerce.order.dto.response.OrderItemResponse;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderHistory;
import com.roze.nexacommerce.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ModelMapper modelMapper;
    private final OrderAddressMapper orderAddressMapper;

    public OrderResponse toResponse(Order order) {
        OrderResponse response = modelMapper.map(order, OrderResponse.class);
        
        // Set customer info
        if (order.getCustomer() != null) {
            response.setCustomerId(order.getCustomer().getId());
            if (order.getCustomer().getUser() != null) {
                response.setCustomerName(order.getCustomer().getUser().getName());
                response.setCustomerEmail(order.getCustomer().getUser().getEmail());
            }
        }
        
        // Set vendor info
        if (order.getVendor() != null) {
            response.setVendorId(order.getVendor().getId());
            response.setVendorName(order.getVendor().getCompanyName());
        }
        
        // Set processed by info
        if (order.getProcessedBy() != null) {
            response.setProcessedByUserId(order.getProcessedBy().getId());
            response.setProcessedByName(order.getProcessedBy().getName());
        }
        
        // Map addresses
        response.setShippingAddress(orderAddressMapper.toResponse(order.getShippingAddress()));
        response.setBillingAddress(orderAddressMapper.toResponse(order.getBillingAddress()));
        
        // Map items and history
        if (order.getOrderItems() != null) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }
        
        if (order.getHistory() != null) {
            List<OrderHistoryResponse> historyResponses = order.getHistory().stream()
                    .map(this::toHistoryResponse)
                    .collect(Collectors.toList());
            response.setHistory(historyResponses);
        }
        
        return response;
    }

    public OrderItemResponse toItemResponse(OrderItem orderItem) {
        OrderItemResponse response = modelMapper.map(orderItem, OrderItemResponse.class);

        // Set product ID
        if (orderItem.getProduct() != null) {
            response.setProductId(orderItem.getProduct().getId());
        }
        // Set computed fields
        response.setSubtotal(orderItem.getSubtotal());
        response.setDiscountAmount(orderItem.getDiscountAmount());
        
        return response;
    }

    public List<OrderItemResponse> toItemResponseList(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    public OrderHistoryResponse toHistoryResponse(OrderHistory orderHistory) {
        OrderHistoryResponse response = modelMapper.map(orderHistory, OrderHistoryResponse.class);
        
        // Set performed by info
        if (orderHistory.getPerformedBy() != null) {
            response.setPerformedByUserId(orderHistory.getPerformedBy().getId());
            response.setPerformedByName(orderHistory.getPerformedBy().getName());
        }
        
        return response;
    }

    public List<OrderHistoryResponse> toHistoryResponseList(List<OrderHistory> orderHistory) {
        return orderHistory.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }
}