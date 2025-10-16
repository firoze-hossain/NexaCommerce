package com.roze.nexacommerce.customer.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.WishlistRequest;
import com.roze.nexacommerce.customer.dto.response.WishlistResponse;
import com.roze.nexacommerce.customer.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers/{customerId}/wishlist")
@RequiredArgsConstructor
public class WishlistController extends BaseController {
    private final WishlistService wishlistService;

    @PostMapping
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<WishlistResponse>> addToWishlist(
            @PathVariable Long customerId,
            @Valid @RequestBody WishlistRequest request) {
        WishlistResponse response = wishlistService.addToWishlist(customerId, request);
        return created(response, "Product added to wishlist successfully");
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<Void>> removeFromWishlist(
            @PathVariable Long customerId,
            @PathVariable Long productId) {
        wishlistService.removeFromWishlist(customerId, productId);
        return noContent("Product removed from wishlist successfully");
    }

    @GetMapping
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<PaginatedResponse<WishlistResponse>>> getWishlist(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<WishlistResponse> wishlist = wishlistService.getCustomerWishlist(customerId, pageable);
        return paginated(wishlist, "Wishlist retrieved successfully");
    }

    @GetMapping("/products/{productId}/exists")
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<Boolean>> checkProductInWishlist(
            @PathVariable Long customerId,
            @PathVariable Long productId) {
        boolean exists = wishlistService.isProductInWishlist(customerId, productId);
        return ok(exists, "Product existence in wishlist checked successfully");
    }

    @GetMapping("/count")
    @PreAuthorize("@securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<Long>> getWishlistCount(@PathVariable Long customerId) {
        Long count = wishlistService.getWishlistCount(customerId);
        return ok(count, "Wishlist count retrieved successfully");
    }
}