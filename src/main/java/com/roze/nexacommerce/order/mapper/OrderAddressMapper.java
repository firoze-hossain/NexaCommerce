package com.roze.nexacommerce.order.mapper;

import com.roze.nexacommerce.order.dto.response.AddressResponse;
import com.roze.nexacommerce.order.entity.ShippingAddress;
import com.roze.nexacommerce.order.entity.BillingAddress;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAddressMapper {
    private final ModelMapper modelMapper;

    public AddressResponse toResponse(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }

        AddressResponse response = modelMapper.map(shippingAddress, AddressResponse.class);
        response.setAddressType("SHIPPING");
        return response;
    }

    public AddressResponse toResponse(BillingAddress billingAddress) {
        if (billingAddress == null) {
            return null;
        }

        AddressResponse response = modelMapper.map(billingAddress, AddressResponse.class);
        response.setAddressType("BILLING");
        return response;
    }
}