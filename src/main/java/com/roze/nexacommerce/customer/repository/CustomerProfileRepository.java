package com.roze.nexacommerce.customer.repository;

import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByUserId(Long userId);

    Optional<CustomerProfile> findByUserEmail(String email);

    @Query("SELECT c FROM CustomerProfile c WHERE c.status = :status")
    Page<CustomerProfile> findByStatus(CustomerStatus status, Pageable pageable);
}