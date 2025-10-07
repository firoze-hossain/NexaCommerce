package com.roze.nexacommerce.cart.enums;

public enum CartType {
    CUSTOMER,    // Regular customer cart
    ADMIN,       // Admin creating order for customer
    GUEST,       // Guest/offline cart
    QUOTE        // Price quote/saved cart
}