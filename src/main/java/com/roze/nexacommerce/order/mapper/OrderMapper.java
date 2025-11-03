package com.roze.nexacommerce.order.mapper;

import com.roze.nexacommerce.order.dto.response.OrderItemResponse;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.entity.Order;
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

    public OrderResponse toResponse(Order order) {
        OrderResponse response = modelMapper.map(order, OrderResponse.class);

        // Map order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        // Set customer info for registered users
        if (order.getCustomer() != null) {
            response.setCustomerId(order.getCustomer().getId());
            if (order.getCustomer().getUser() != null) {
                response.setCustomerName(order.getCustomer().getUser().getName());
                response.setCustomerEmail(order.getCustomer().getUser().getEmail());
            }
        } else {
            // Set guest info for guest orders
            response.setCustomerName(order.getGuestName());
            response.setCustomerEmail(order.getGuestEmail());
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

        return response;
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = modelMapper.map(orderItem, OrderItemResponse.class);

        // Ensure product details are set
        if (orderItem.getProduct() != null) {
            response.setProductId(orderItem.getProduct().getId());
            response.setProductName(orderItem.getProductName() != null ?
                    orderItem.getProductName() : orderItem.getProduct().getName());
            response.setProductSku(orderItem.getProductSku() != null ?
                    orderItem.getProductSku() : orderItem.getProduct().getSku());

            // Set product image if available
            if (orderItem.getProductImage() == null &&
                    orderItem.getProduct().getImages() != null &&
                    !orderItem.getProduct().getImages().isEmpty()) {
                response.setProductImage(orderItem.getProduct().getImages().get(0).getImageUrl());
            }
        }

        return response;
    }
}