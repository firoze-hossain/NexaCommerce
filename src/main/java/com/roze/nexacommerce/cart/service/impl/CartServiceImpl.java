package com.roze.nexacommerce.cart.service.impl;


import com.roze.nexacommerce.cart.dto.request.CartItemRequest;
import com.roze.nexacommerce.cart.dto.response.CartResponse;
import com.roze.nexacommerce.cart.entity.Cart;
import com.roze.nexacommerce.cart.entity.CartItem;
import com.roze.nexacommerce.cart.enums.CartType;
import com.roze.nexacommerce.cart.mapper.CartMapper;
import com.roze.nexacommerce.cart.repository.CartItemRepository;
import com.roze.nexacommerce.cart.repository.CartRepository;
import com.roze.nexacommerce.cart.service.CartService;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerProfileRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional//(readOnly = true)
    public CartResponse getCartByCustomer(Long customerId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> createNewCustomerCart(customer));

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional //(readOnly = true)
    public CartResponse getCartBySession(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewGuestCart(sessionId));

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(Long customerId, CartItemRequest request) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> createNewCustomerCart(customer));

        return addOrUpdateCartItem(cart, request);
    }

    @Override
    @Transactional
    public CartResponse addItemToGuestCart(String sessionId, CartItemRequest request) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewGuestCart(sessionId));

        return addOrUpdateCartItem(cart, request);
    }

    //    private CartResponse addOrUpdateCartItem(Cart cart, CartItemRequest request) {
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
//
//        if (!product.isAvailable()) {
//            throw new IllegalStateException("Product is not available for purchase");
//        }
//
//        if (product.getTrackQuantity() && !product.getAllowBackorder() &&
//            product.getStock() < request.getQuantity()) {
//            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
//        }
//
//        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
//
//        if (existingItem.isPresent()) {
//            CartItem item = existingItem.get();
//            item.updateQuantity(item.getQuantity() + request.getQuantity());
//            cartItemRepository.save(item);
//        } else {
//            CartItem newItem = CartItem.builder()
//                    .cart(cart)
//                    .product(product)
//                    .quantity(request.getQuantity())
//                    .price(product.getPrice())
//                    .compareAtPrice(product.getCompareAtPrice())
//                    .build();
//            cart.addItem(newItem);
//            cartItemRepository.save(newItem);
//        }
//
//        cartRepository.save(cart);
//        return cartMapper.toResponse(cart);
//    }
    private CartResponse addOrUpdateCartItem(Cart cart, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isAvailable()) {
            throw new IllegalStateException("Product is not available for purchase");
        }

        if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                product.getStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + request.getQuantity());
            // No need to call save() - @Transactional will handle it
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .compareAtPrice(product.getCompareAtPrice())
                    .build();
            cart.addItem(newItem);
            // DO NOT call cartItemRepository.save(newItem) here
        }

        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long customerId, Long productId, Integer quantity) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        return updateCartItemQuantity(cart, productId, quantity);
    }

    @Override
    @Transactional
    public CartResponse updateGuestCartItem(String sessionId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        return updateCartItemQuantity(cart, productId, quantity);
    }

    private CartResponse updateCartItemQuantity(Cart cart, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "productId", productId));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            if (product.getTrackQuantity() && !product.getAllowBackorder() && product.getStock() < quantity) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            item.updateQuantity(quantity);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(Long customerId, Long productId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        return removeCartItem(cart, productId);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromGuestCart(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        return removeCartItem(cart, productId);
    }

    private CartResponse removeCartItem(Cart cart, Long productId) {
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "productId", productId));

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.clearCart();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearGuestCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.clearCart();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponse mergeCarts(Long customerId, String sessionId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart customerCart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> createNewCustomerCart(customer));

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);

        if (guestCartOpt.isPresent()) {
            Cart guestCart = guestCartOpt.get();
            List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

            for (CartItem guestItem : guestItems) {
                Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
                        customerCart.getId(), guestItem.getProduct().getId());

                if (existingItem.isPresent()) {
                    CartItem item = existingItem.get();
                    item.incrementQuantity(guestItem.getQuantity());
                    cartItemRepository.save(item);
                } else {
                    CartItem newItem = CartItem.builder()
                            .cart(customerCart)
                            .product(guestItem.getProduct())
                            .quantity(guestItem.getQuantity())
                            .price(guestItem.getPrice())
                            .compareAtPrice(guestItem.getCompareAtPrice())
                            .build();
                    customerCart.addItem(newItem);
                    cartItemRepository.save(newItem);
                }
            }

            cartItemRepository.deleteByCartId(guestCart.getId());
            cartRepository.delete(guestCart);
        }

        cartRepository.save(customerCart);
        return cartMapper.toResponse(customerCart);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCart(Long customerId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (!product.isAvailable()) {
                return false;
            }
            if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                    product.getStock() < item.getQuantity()) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartSummary(Long customerId) {
        return getCartByCustomer(customerId);
    }

    private Cart createNewCustomerCart(CustomerProfile customer) {
        Cart cart = Cart.builder()
                .customer(customer)
                .type(CartType.CUSTOMER)
                .isActive(true)
                .isSaved(false)
                .build();
        return cartRepository.save(cart);
    }

    private Cart createNewGuestCart(String sessionId) {
        Cart cart = Cart.builder()
                .sessionId(sessionId)
                .type(CartType.GUEST)
                .isActive(true)
                .isSaved(false)
                .build();
        return cartRepository.save(cart);
    }
}