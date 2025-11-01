package com.roze.nexacommerce.common.address.service.impl;

import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.dto.response.LocationDataResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.common.address.mapper.AddressMapper;
import com.roze.nexacommerce.common.address.repository.AddressRepository;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
        AddressResponse response = addressMapper.toResponse(savedAddress);
        log.info("Address created successfully");
        return response;
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
    public AddressResponse getDefaultAddress(Long userId) {
        // Get any default address regardless of type
        return addressRepository.findByUserIdAndIsDefault(userId, true)
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

    @Override
    public Address getAddressEntityById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
    }

    public LocationDataResponse getLocationData() {
        List<String> dhakaMetroAreas = Arrays.asList(
                "Gulshan", "Banani", "Baridhara", "Bashundhara", "Uttara",
                "Dhanmondi", "Mirpur", "Mohammadpur", "Motijheel", "Malibagh",
                "Rampura", "Badda", "Mohakhali", "Farmgate", "Shyamoli"
        );

        List<String> dhakaSuburbanAreas = Arrays.asList(
                "Savar", "Keraniganj", "Narayanganj", "Gazipur", "Tongi"
        );

        List<String> otherCities = Arrays.asList(
                "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal",
                "Rangpur", "Mymensingh", "Comilla"
        );

        Map<String, List<String>> cityAreas = new HashMap<>();
        cityAreas.put("Dhaka", dhakaMetroAreas);
        cityAreas.put("Dhaka Suburbs", dhakaSuburbanAreas);

        List<LocationDataResponse.ShippingRate> shippingRates = Arrays.asList(
                LocationDataResponse.ShippingRate.builder()
                        .zone("Inside Dhaka")
                        .rate(new BigDecimal("60"))
                        .deliveryTime("1-2 days")
                        .build(),
                LocationDataResponse.ShippingRate.builder()
                        .zone("Outside Dhaka")
                        .rate(new BigDecimal("120"))
                        .deliveryTime("3-5 days")
                        .build()
        );

        return LocationDataResponse.builder()
                .dhakaMetroAreas(dhakaMetroAreas)
                .dhakaSuburbanAreas(dhakaSuburbanAreas)
                .otherCities(otherCities)
                .cityAreas(cityAreas)
                .shippingRates(shippingRates)
                .build();
    }
}