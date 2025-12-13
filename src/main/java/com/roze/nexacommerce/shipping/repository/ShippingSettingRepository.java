package com.roze.nexacommerce.shipping.repository;

import com.roze.nexacommerce.shipping.entity.ShippingSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingSettingRepository extends JpaRepository<ShippingSetting, Long> {

    Optional<ShippingSetting> findByLocationType(String locationType);

    Optional<ShippingSetting> findByLocationTypeAndActiveTrue(String locationType);

    boolean existsByLocationType(String locationType);

    List<ShippingSetting> findByActiveTrue();
}