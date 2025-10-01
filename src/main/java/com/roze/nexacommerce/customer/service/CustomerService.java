package com.roze.nexacommerce.customer.service;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.CustomerRegistrationRequest;
import com.roze.nexacommerce.customer.dto.request.CustomerUpdateRequest;
import com.roze.nexacommerce.customer.dto.response.CustomerDetailResponse;
import com.roze.nexacommerce.customer.dto.response.CustomerResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerDetailResponse register(CustomerRegistrationRequest request);

    CustomerDetailResponse getCustomerById(Long customerId);

    CustomerDetailResponse getCustomerByUserId(Long userId);

    CustomerDetailResponse getCustomerByEmail(String email);

    PaginatedResponse<CustomerResponse> getAllCustomers(Pageable pageable);

    CustomerDetailResponse updateCustomer(Long customerId, CustomerUpdateRequest request);

    CustomerDetailResponse updateCustomerByUserId(Long userId, CustomerUpdateRequest request);

    void deleteCustomer(Long customerId);
}