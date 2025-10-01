package com.roze.nexacommerce.common.address.service;

import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.enums.AddressType;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(Long userId, AddressRequest request);
    List<AddressResponse> getUserAddresses(Long userId);
    List<AddressResponse> getUserAddressesByType(Long userId, AddressType addressType);
    AddressResponse getAddressById(Long addressId);
    AddressResponse getDefaultAddress(Long userId, AddressType addressType);
    AddressResponse updateAddress(Long addressId, AddressRequest request);
    AddressResponse setDefaultAddress(Long addressId);
    void deleteAddress(Long addressId);
}