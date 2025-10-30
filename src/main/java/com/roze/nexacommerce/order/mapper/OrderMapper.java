package com.roze.nexacommerce.order.mapper;

import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ModelMapper modelMapper;

    public OrderResponse toResponse(Order order) {
        OrderResponse response = modelMapper.map(order, OrderResponse.class);

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
}