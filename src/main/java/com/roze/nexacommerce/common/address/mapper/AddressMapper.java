package com.roze.nexacommerce.common.address.mapper;

import com.roze.nexacommerce.common.address.dto.request.AddressRequest;
import com.roze.nexacommerce.common.address.dto.response.AddressResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressMapper {
    private final ModelMapper modelMapper;

    public Address toEntity(AddressRequest request) {
        return modelMapper.map(request, Address.class);
    }

    public AddressResponse toResponse(Address address) {
        return modelMapper.map(address, AddressResponse.class);
    }

    public void updateEntity(AddressRequest request, Address address) {
        modelMapper.map(request, address);
    }
}