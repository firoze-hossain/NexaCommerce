package com.roze.nexacommerce.cart.repository;

import com.roze.nexacommerce.cart.entity.Cart;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(CustomerProfile customer);

    Optional<Cart> findByCustomerId(Long customerId);

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    List<Cart> findByUserIdAndIsSavedTrue(Long userId);

    List<Cart> findByTypeAndIsActiveTrue(com.roze.nexacommerce.cart.enums.CartType type);

    boolean existsByCustomerId(Long customerId);

    boolean existsBySessionId(String sessionId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.sessionId = :sessionId AND c.customer IS NULL")
    void deleteGuestCartsBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT c FROM Cart c WHERE c.isActive = true AND c.updatedAt < :cutoffDate")
    List<Cart> findAbandonedCarts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}