package com.roze.nexacommerce.customer.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.CustomerRegistrationRequest;
import com.roze.nexacommerce.customer.dto.request.CustomerUpdateRequest;
import com.roze.nexacommerce.customer.dto.response.CustomerDetailResponse;
import com.roze.nexacommerce.customer.dto.response.CustomerResponse;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.mapper.CustomerMapper;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.customer.service.CustomerService;
import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.RoleRepository;
import com.roze.nexacommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerProfileRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerDetailResponse register(CustomerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "CUSTOMER"));

        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        CustomerProfile customer = customerMapper.toEntity(request, savedUser);
        CustomerProfile savedCustomer = customerRepository.save(customer);

        return customerMapper.toDetailResponse(savedCustomer);
    }

    @Override
    public CustomerDetailResponse getCustomerById(Long customerId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return customerMapper.toDetailResponse(customer);
    }

    @Override
    public CustomerDetailResponse getCustomerByUserId(Long userId) {
        CustomerProfile customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "user_id", userId));
        return customerMapper.toDetailResponse(customer);
    }

    @Override
    public CustomerDetailResponse getCustomerByEmail(String email) {
        CustomerProfile customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return customerMapper.toDetailResponse(customer);
    }

    @Override
    public PaginatedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<CustomerProfile> customerPage = customerRepository.findAll(pageable);
        List<CustomerResponse> customerResponses = customerPage.getContent()
                .stream()
                .map(customerMapper::toResponse)
                .toList();

        return PaginatedResponse.<CustomerResponse>builder()
                .items(customerResponses)
                .totalItems(customerPage.getTotalElements())
                .currentPage(customerPage.getNumber())
                .pageSize(customerPage.getSize())
                .totalPages(customerPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public CustomerDetailResponse updateCustomer(Long customerId, CustomerUpdateRequest request) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customerMapper.updateEntity(request, customer);
        CustomerProfile updatedCustomer = customerRepository.save(customer);

        return customerMapper.toDetailResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public CustomerDetailResponse updateCustomerByUserId(Long userId, CustomerUpdateRequest request) {
        CustomerProfile customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "user_id", userId));

        customerMapper.updateEntity(request, customer);
        CustomerProfile updatedCustomer = customerRepository.save(customer);

        return customerMapper.toDetailResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

       // userRepository.delete(customer.getUser());
        customerRepository.delete(customer);
    }
}