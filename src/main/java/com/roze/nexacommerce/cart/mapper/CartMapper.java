package com.roze.nexacommerce.cart.mapper;


import com.roze.nexacommerce.cart.dto.response.CartItemResponse;
import com.roze.nexacommerce.cart.dto.response.CartResponse;
import com.roze.nexacommerce.cart.entity.Cart;
import com.roze.nexacommerce.cart.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final ModelMapper modelMapper;

    public CartResponse toResponse(Cart cart) {
        CartResponse response = modelMapper.map(cart, CartResponse.class);

        // Set computed fields
        response.setTotalItems(cart.getTotalItems());
        response.setTotalUniqueItems(cart.getTotalUniqueItems());
        response.setTotalAmount(cart.getTotalAmount());
        response.setTotalDiscount(cart.getTotalDiscount());

        // Map items
        if (cart.getCartItems() != null) {
            List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }

    public CartItemResponse toItemResponse(CartItem cartItem) {
        CartItemResponse response = modelMapper.map(cartItem, CartItemResponse.class);

        // Set product info
        if (cartItem.getProduct() != null) {
            response.setProductId(cartItem.getProduct().getId());
            response.setProductName(cartItem.getProduct().getName());
            response.setProductSku(cartItem.getProduct().getSku());
            response.setProductImage(getPrimaryImage(cartItem.getProduct()));
            response.setAvailableStock(cartItem.getProduct().getStock());
            response.setInStock(cartItem.getProduct().isInStock());
            response.setLowStock(cartItem.getProduct().isLowStock());
        }

        // Set computed fields
        response.setSubtotal(cartItem.getSubtotal());
        response.setDiscountAmount(cartItem.getDiscountAmount());

        return response;
    }

    public List<CartItemResponse> toItemResponseList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    private String getPrimaryImage(com.roze.nexacommerce.product.entity.Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(com.roze.nexacommerce.product.entity.ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl());
    }
}