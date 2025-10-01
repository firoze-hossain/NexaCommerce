package com.roze.nexacommerce.customer.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.WishlistRequest;
import com.roze.nexacommerce.customer.dto.response.WishlistResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistResponse addToWishlist(Long customerId, WishlistRequest request);

//    void removeFromWishlist(Long customerId, Long productId);

    PaginatedResponse<WishlistResponse> getCustomerWishlist(Long customerId, Pageable pageable);

//    boolean isProductInWishlist(Long customerId, Long productId);

    Long getWishlistCount(Long customerId);
}