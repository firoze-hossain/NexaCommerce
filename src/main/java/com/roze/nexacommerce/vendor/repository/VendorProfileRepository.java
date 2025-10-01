package com.roze.nexacommerce.vendor.repository;

import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);

    Optional<VendorProfile> findByBusinessEmail(String businessEmail);

    boolean existsByCompanyName(String companyName);

    boolean existsByBusinessEmail(String businessEmail);

    @Query("SELECT v FROM VendorProfile v WHERE v.status = :status")
    Page<VendorProfile> findByStatus(VendorStatus status, Pageable pageable);

    @Query("SELECT v FROM VendorProfile v WHERE v.user.email = :email")
    Optional<VendorProfile> findByUserEmail(String email);
}