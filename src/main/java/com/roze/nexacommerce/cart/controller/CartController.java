package com.roze.nexacommerce.cart.controller;

import com.roze.nexacommerce.cart.dto.request.CartItemRequest;
import com.roze.nexacommerce.cart.dto.response.CartResponse;
import com.roze.nexacommerce.cart.service.CartService;
import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.security.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController extends BaseController {
    private final CartService cartService;
    private final SecurityService securityService;

    @GetMapping("/my-cart")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<CartResponse>> getMyCart() {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        CartResponse response = cartService.getCartByCustomer(customerId);
        return ok(response, "Cart retrieved successfully");
    }

    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<BaseResponse<CartResponse>> getGuestCart(@PathVariable String sessionId) {
        CartResponse response = cartService.getCartBySession(sessionId);
        return ok(response, "Guest cart retrieved successfully");
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('CREATE_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody CartItemRequest request) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        CartResponse response = cartService.addItemToCart(customerId, request);
        return ok(response, "Item added to cart successfully");
    }

    @PostMapping("/guest/{sessionId}/items")
    public ResponseEntity<BaseResponse<CartResponse>> addItemToGuestCart(
            @PathVariable String sessionId,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItemToGuestCart(sessionId, request);
        return ok(response, "Item added to guest cart successfully");
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<CartResponse>> updateCartItem(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        CartResponse response = cartService.updateCartItem(customerId, productId, quantity);
        return ok(response, "Cart item updated successfully");
    }

    @PutMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<BaseResponse<CartResponse>> updateGuestCartItem(
            @PathVariable String sessionId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        CartResponse response = cartService.updateGuestCartItem(sessionId, productId, quantity);
        return ok(response, "Guest cart item updated successfully");
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<CartResponse>> removeItemFromCart(
            @PathVariable Long productId) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        CartResponse response = cartService.removeItemFromCart(customerId, productId);
        return ok(response, "Item removed from cart successfully");
    }

    @DeleteMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<BaseResponse<CartResponse>> removeItemFromGuestCart(
            @PathVariable String sessionId,
            @PathVariable Long productId) {
        CartResponse response = cartService.removeItemFromGuestCart(sessionId, productId);
        return ok(response, "Item removed from guest cart successfully");
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<Void>> clearCart() {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        cartService.clearCart(customerId);
        return noContent("Cart cleared successfully");
    }

    @DeleteMapping("/guest/{sessionId}/clear")
    public ResponseEntity<BaseResponse<Void>> clearGuestCart(@PathVariable String sessionId) {
        cartService.clearGuestCart(sessionId);
        return noContent("Guest cart cleared successfully");
    }

    @PostMapping("/merge/{sessionId}")
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<CartResponse>> mergeCarts(@PathVariable String sessionId) {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        CartResponse response = cartService.mergeCarts(customerId, sessionId);
        return ok(response, "Carts merged successfully");
    }

    @GetMapping("/validate")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<Boolean>> validateCart() {
        Long customerId = securityService.getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        boolean isValid = cartService.validateCart(customerId);
        return ok(isValid, isValid ? "Cart is valid" : "Cart contains invalid items");
    }

}