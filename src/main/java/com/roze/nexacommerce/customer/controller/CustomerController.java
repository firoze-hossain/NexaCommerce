package com.roze.nexacommerce.customer.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.customer.dto.request.CustomerRegistrationRequest;
import com.roze.nexacommerce.customer.dto.request.CustomerUpdateRequest;
import com.roze.nexacommerce.customer.dto.response.CustomerDetailResponse;
import com.roze.nexacommerce.customer.dto.response.CustomerResponse;
import com.roze.nexacommerce.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {
    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<CustomerDetailResponse>> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        CustomerDetailResponse response = customerService.register(request);
        return created(response, "Customer registered successfully");
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('READ_CUSTOMER') or @securityService.canAccessCustomer(#customerId)")
    public ResponseEntity<BaseResponse<CustomerDetailResponse>> getCustomerById(@PathVariable Long customerId) {
        CustomerDetailResponse response = customerService.getCustomerById(customerId);
        return ok(response, "Customer retrieved successfully");
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('READ_CUSTOMER') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<CustomerDetailResponse>> getCustomerByUserId(@PathVariable Long userId) {
        CustomerDetailResponse response = customerService.getCustomerByUserId(userId);
        return ok(response, "Customer retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<BaseResponse<PaginatedResponse<CustomerResponse>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<CustomerResponse> customers = customerService.getAllCustomers(pageable);
        return paginated(customers, "Customers retrieved successfully");
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('UPDATE_CUSTOMER')")
    public ResponseEntity<BaseResponse<CustomerDetailResponse>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerDetailResponse response = customerService.updateCustomer(customerId, request);
        return ok(response, "Customer updated successfully");
    }

    @PutMapping("/user/{userId}")
    @PreAuthorize("@securityService.isCurrentUser(#userId)")
    public ResponseEntity<BaseResponse<CustomerDetailResponse>> updateCustomerByUserId(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerDetailResponse response = customerService.updateCustomerByUserId(userId, request);
        return ok(response, "Customer profile updated successfully");
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAuthority('DELETE_CUSTOMER')")
    public ResponseEntity<BaseResponse<Void>> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return noContent("Customer deleted successfully");
    }
}