package com.roze.nexacommerce.customer.mapper;

import com.roze.nexacommerce.customer.dto.request.CustomerRegistrationRequest;
import com.roze.nexacommerce.customer.dto.request.CustomerUpdateRequest;
import com.roze.nexacommerce.customer.dto.response.CustomerDetailResponse;
import com.roze.nexacommerce.customer.dto.response.CustomerResponse;
import com.roze.nexacommerce.customer.dto.response.UserInfo;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMapper {
    private final ModelMapper modelMapper;

    public CustomerProfile toEntity(CustomerRegistrationRequest request, User user) {
        CustomerProfile customer = modelMapper.map(request, CustomerProfile.class);
        customer.setUser(user);
        customer.setPhone(request.getPhone());
        return customer;
    }

    public CustomerResponse toResponse(CustomerProfile customer) {
        return modelMapper.map(customer, CustomerResponse.class);
    }

    public CustomerDetailResponse toDetailResponse(CustomerProfile customer) {
        CustomerResponse customerResponse = toResponse(customer);
        User user = customer.getUser();

        UserInfo userInfo = UserInfo.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .active(user.getActive())
                .build();

        return CustomerDetailResponse.builder()
                .customer(customerResponse)
                .userInfo(userInfo)
                .build();
    }

    public void updateEntity(CustomerUpdateRequest request, CustomerProfile customer) {
        modelMapper.map(request, customer);
    }
}