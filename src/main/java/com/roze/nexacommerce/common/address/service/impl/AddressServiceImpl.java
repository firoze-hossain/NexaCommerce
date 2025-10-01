package com.roze.nexacommerce.common.address.service.impl;

import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.common.address.mapper.AddressMapper;
import com.roze.nexacommerce.common.address.repository.AddressRepository;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // If this is set as default, unset other defaults of same type
        if (request.getIsDefault()) {
            addressRepository.unsetDefaultAddresses(userId, request.getAddressType());
        }

        Address address = addressMapper.toEntity(request);
        address.setUser(user);
        
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return addressRepository.findByUserId(userId)
                .stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressResponse> getUserAddressesByType(Long userId, AddressType addressType) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return addressRepository.findByUserIdAndAddressType(userId, addressType)
                .stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return addressMapper.toResponse(address);
    }

    @Override
    public AddressResponse getDefaultAddress(Long userId, AddressType addressType) {
        return addressRepository.findByUserIdAndAddressTypeAndIsDefault(userId, addressType, true)
                .map(addressMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // If setting as default, unset other defaults
        if (request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.unsetDefaultAddresses(address.getUser().getId(), request.getAddressType());
        }

        addressMapper.updateEntity(request, address);
        Address updatedAddress = addressRepository.save(address);

        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Unset current default
        addressRepository.unsetDefaultAddresses(address.getUser().getId(), address.getAddressType());

        // Set new default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }
}