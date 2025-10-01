package com.roze.nexacommerce.customer.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.WishlistRequest;
import com.roze.nexacommerce.customer.dto.response.WishlistResponse;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.entity.Wishlist;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.customer.repository.WishlistRepository;
import com.roze.nexacommerce.customer.service.WishlistService;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
//import com.roze.nexacommerce.product.entity.Product;
//import com.roze.nexacommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final CustomerProfileRepository customerRepository;
    //private final ProductRepository productRepository;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Long customerId, WishlistRequest request) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if already in wishlist
//        if (wishlistRepository.existsByCustomerIdAndProductId(customerId, request.getProductId())) {
//            throw new DuplicateResourceException("Product already exists in wishlist");
//        }

        Wishlist wishlist = Wishlist.builder()
                .customer(customer)
                //.product(product)
                .notes(request.getNotes())
                .build();

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        
        // Update customer's wishlist count
        customer.incrementWishlistCount();
        customerRepository.save(customer);

        return mapToWishlistResponse(savedWishlist);
    }

//    @Override
//    @Transactional
//    public void removeFromWishlist(Long customerId, Long productId) {
//        Wishlist wishlist = wishlistRepository.findByCustomerIdAndProductId(customerId, productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
//
//        wishlistRepository.delete(wishlist);
//
//        // Update customer's wishlist count
//        CustomerProfile customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
//        customer.decrementWishlistCount();
//        customerRepository.save(customer);
//    }

    @Override
    public PaginatedResponse<WishlistResponse> getCustomerWishlist(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        Page<Wishlist> wishlistPage = wishlistRepository.findByCustomerId(customerId, pageable);
        List<WishlistResponse> wishlistResponses = wishlistPage.getContent()
                .stream()
                .map(this::mapToWishlistResponse)
                .toList();

        return PaginatedResponse.<WishlistResponse>builder()
                .items(wishlistResponses)
                .totalItems(wishlistPage.getTotalElements())
                .currentPage(wishlistPage.getNumber())
                .pageSize(wishlistPage.getSize())
                .totalPages(wishlistPage.getTotalPages())
                .build();
    }

//    @Override
//    public boolean isProductInWishlist(Long customerId, Long productId) {
//        return wishlistRepository.existsByCustomerIdAndProductId(customerId, productId);
//    }

    @Override
    public Long getWishlistCount(Long customerId) {
        return wishlistRepository.countByCustomerId(customerId);
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
     //   Product product = wishlist.getProduct();
        
        return WishlistResponse.builder()
                .id(wishlist.getId())
//                .productId(product.getId())
//                .productName(product.getName())
//                .productPrice(product.getPrice())
//                .productImage(getFirstProductImage(product))
                .notes(wishlist.getNotes())
                .addedAt(wishlist.getCreatedAt())
                .build();
    }

//    private String getFirstProductImage(Product product) {
//        // This would depend on your product image implementation
//        return product.getImages() != null && !product.getImages().isEmpty()
//                ? product.getImages().get(0).getImageUrl()
//                : null;
//    }
}