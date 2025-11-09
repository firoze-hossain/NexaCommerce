package com.roze.nexacommerce.order.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.order.dto.request.GuestOrderCreateRequest;
import com.roze.nexacommerce.order.dto.request.OrderCreateRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.service.OrderService;
import com.roze.nexacommerce.order.service.ReceiptService;
import com.roze.nexacommerce.security.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController extends BaseController {
    private final OrderService orderService;
    private final SecurityService securityService;
    private final ReceiptService receiptService;
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

    // Guest order creation
    @PostMapping("/guest")
    public ResponseEntity<BaseResponse<OrderResponse>> createGuestOrder(
            @Valid @RequestBody GuestOrderCreateRequest request) {
        OrderResponse response = orderService.createGuestOrder(request);
        return created(response, "Order created successfully");
    }

    // Get guest order by number and email
    @GetMapping("/guest/{orderNumber}")
    public ResponseEntity<BaseResponse<OrderResponse>> getGuestOrder(
            @PathVariable String orderNumber,
            @RequestParam String email) {
        OrderResponse response = orderService.getGuestOrder(orderNumber, email);
        return ok(response, "Order retrieved successfully");
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isOrderOwner(#orderId) or @securityService.isVendorOrder(#orderId) or @securityService.isOrderOwnerOrHasAccess(#orderId) or @securityService.isVendorOrder(#orderId)")
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

    //    @GetMapping("/my-orders")
//    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer() or @securityService.canAccessOrders()")
//    public ResponseEntity<BaseResponse<PaginatedResponse<OrderResponse>>> getMyOrders(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDirection) {
//        Long customerId = getCurrentCustomerId();
//        if (customerId == null) {
//            return unauthorized("Customer not found");
//        }
//
//        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        PaginatedResponse<OrderResponse> orders = orderService.getCustomerOrders(customerId, pageable);
//        return paginated(orders, "Orders retrieved successfully");
//    }
    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isCurrentCustomer() or @securityService.canAccessOrders()")
    public ResponseEntity<BaseResponse<PaginatedResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<OrderResponse> orders;

        // Check if user is admin/superadmin/vendor (can access all orders)
        if (securityService.canAccessOrders()) {
            orders = orderService.getAllOrders(pageable);
        }
        // Check if user is a customer
        else {
            Long customerId = getCurrentCustomerId();
            if (customerId == null) {
                return unauthorized("Customer not found");
            }
            orders = orderService.getCustomerOrders(customerId, pageable);
        }

        return paginated(orders, "Orders retrieved successfully");
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('UPDATE_ORDER') or @securityService.isVendorOrder(#orderId)")
    public ResponseEntity<BaseResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ok(response, "Order status updated successfully");
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

//    @GetMapping("/{orderId}/receipt")
//    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isOrderOwner(#orderId) or @securityService.isVendorOrder(#orderId)")
//    public ResponseEntity<byte[]> downloadOrderReceipt(@PathVariable Long orderId) {
//        byte[] pdfContent = receiptService.generateOrderReceipt(orderId);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"order-receipt-" + orderId + ".pdf\"")
//                .body(pdfContent);
//    }

    @GetMapping("/number/{orderNumber}/receipt")
    @PreAuthorize("hasAuthority('READ_ORDER') or @securityService.isOrderOwnerByNumber(#orderNumber) or @securityService.isVendorOrderByNumber(#orderNumber)")
    public ResponseEntity<byte[]> downloadOrderReceiptByNumber(@PathVariable String orderNumber) {
        byte[] pdfContent = receiptService.generateOrderReceipt(orderNumber);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"order-receipt-" + orderNumber + ".pdf\"")
                .body(pdfContent);
    }

    // Guest order receipt (no authentication required, but validate email)
    @GetMapping("/guest/receipt/{orderNumber}")
    public ResponseEntity<byte[]> downloadGuestOrderReceipt(
            @PathVariable String orderNumber,
            @RequestParam String phoneNumber) {

        // Validate guest order access
        orderService.validateGuestOrderAccess(orderNumber, phoneNumber);

        byte[] pdfContent = receiptService.generateOrderReceipt(orderNumber);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"order-receipt-" + orderNumber + ".pdf\"")
                .body(pdfContent);
    }
    private Long getCurrentCustomerId() {
        return securityService.getCurrentCustomerId();
    }
}