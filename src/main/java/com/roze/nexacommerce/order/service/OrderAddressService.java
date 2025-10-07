package com.roze.nexacommerce.order.service;

import com.roze.nexacommerce.order.entity.BillingAddress;
import com.roze.nexacommerce.order.entity.ShippingAddress;

public interface OrderAddressService {
    ShippingAddress createShippingAddress(Long addressId);

    BillingAddress createBillingAddress(Long addressId);

    BillingAddress createBillingAddressFromShipping(ShippingAddress shippingAddress);
}
