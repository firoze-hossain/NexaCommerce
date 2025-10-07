package com.roze.nexacommerce.order.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.order.dto.request.OrderCreateRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import com.roze.nexacommerce.order.service.OrderService;
import com.roze.nexacommerce.security.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController extends BaseController {
    private final OrderService orderService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        Long customerId = getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }
        OrderResponse response = orderService.createOrder(customerId, request);
        return created(response, "Order created successfully");
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isOrderOwner(#orderId) or @securityService.isVendorOrder(#orderId)")
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ok(response, "Order retrieved successfully");
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isOrderOwnerByNumber(#orderNumber) or @securityService.isVendorOrderByNumber(#orderNumber)")
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ok(response, "Order retrieved successfully");
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<PaginatedResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Long customerId = getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<OrderResponse> orders = orderService.getCustomerOrders(customerId, pageable);
        return paginated(orders, "Orders retrieved successfully");
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer()")
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        Long customerId = getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }

        var response = orderService.getRecentOrders(customerId, limit);
        return ok(response, "Recent orders retrieved successfully");
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isVendorOrder(#orderId)")
    public ResponseEntity<BaseResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ok(response, "Order status updated successfully");
    }

    @PatchMapping("/{orderId}/payment-status")
    @PreAuthorize("hasAuthority('PROCESS_PAYMENT')")
    public ResponseEntity<BaseResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus) {
        OrderResponse response = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ok(response, "Payment status updated successfully");
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isOrderOwner(#orderId)")
    public ResponseEntity<BaseResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        Long customerId = getCurrentCustomerId();
        if (customerId == null) {
            return unauthorized("Customer not found");
        }

        OrderResponse response = orderService.cancelOrder(orderId, customerId);
        return ok(response, "Order cancelled successfully");
    }

    @PostMapping("/{orderId}/notes")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isVendorOrder(#orderId)")
    public ResponseEntity<BaseResponse<OrderResponse>> addOrderNote(
            @PathVariable Long orderId,
            @RequestParam String note) {
        OrderResponse response = orderService.addOrderNote(orderId, note);
        return ok(response, "Order note added successfully");
    }


    private Long getCurrentCustomerId() {
        return securityService.getCurrentCustomerId();
    }
}