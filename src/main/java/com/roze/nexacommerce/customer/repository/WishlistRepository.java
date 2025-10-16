package com.roze.nexacommerce.customer.repository;

import com.roze.nexacommerce.customer.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Wishlist> findByCustomerIdAndProductId(Long customerId, Long productId);

    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.customer.id = :customerId AND w.product.id = :productId")
    void deleteByCustomerIdAndProductId(Long customerId, Long productId);

    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.customer.id = :customerId")
    Long countByCustomerId(Long customerId);
}