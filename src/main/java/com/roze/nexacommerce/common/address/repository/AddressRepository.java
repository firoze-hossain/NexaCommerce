package com.roze.nexacommerce.common.address.repository;

import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    List<Address> findByUserIdAndAddressType(Long userId, AddressType addressType);
    Optional<Address> findByUserIdAndAddressTypeAndIsDefault(Long userId, AddressType addressType, Boolean isDefault);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.addressType = :addressType")
    void unsetDefaultAddresses(@Param("userId") Long userId, @Param("addressType") AddressType addressType);
    
    boolean existsByUserIdAndAddressTypeAndIsDefault(Long userId, AddressType addressType, Boolean isDefault);
}