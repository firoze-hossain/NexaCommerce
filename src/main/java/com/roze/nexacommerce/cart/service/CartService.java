package com.roze.nexacommerce.cart.service;


import com.roze.nexacommerce.cart.dto.request.CartItemRequest;
import com.roze.nexacommerce.cart.dto.response.CartResponse;

public interface CartService {
    CartResponse getCartByCustomer(Long customerId);

    CartResponse getCartBySession(String sessionId);

    CartResponse addItemToCart(Long customerId, CartItemRequest request);

    CartResponse addItemToGuestCart(String sessionId, CartItemRequest request);

    CartResponse updateCartItem(Long customerId, Long productId, Integer quantity);

    CartResponse updateGuestCartItem(String sessionId, Long productId, Integer quantity);

    CartResponse removeItemFromCart(Long customerId, Long productId);

    CartResponse removeItemFromGuestCart(String sessionId, Long productId);

    void clearCart(Long customerId);

    void clearGuestCart(String sessionId);

    CartResponse mergeCarts(Long customerId, String sessionId);

    boolean validateCart(Long customerId);

    CartResponse getCartSummary(Long customerId);
}