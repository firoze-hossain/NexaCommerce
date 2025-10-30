//package com.roze.nexacommerce.order.controller;
//
//import com.roze.nexacommerce.common.BaseController;
//import com.roze.nexacommerce.common.BaseResponse;
//import com.roze.nexacommerce.common.PaginatedResponse;
//import com.roze.nexacommerce.order.dto.request.ManualOrderRequest;
//import com.roze.nexacommerce.order.dto.request.OrderSearchCriteria;
//import com.roze.nexacommerce.order.dto.request.RefundRequest;
//import com.roze.nexacommerce.order.dto.response.OrderResponse;
//import com.roze.nexacommerce.order.dto.response.OrderStatsResponse;
//import com.roze.nexacommerce.order.enums.OrderStatus;
//import com.roze.nexacommerce.order.enums.PaymentStatus;
//import com.roze.nexacommerce.order.service.AdminOrderService;
//import com.roze.nexacommerce.security.SecurityService;
//import com.roze.nexacommerce.user.entity.User;
//import com.roze.nexacommerce.user.repository.UserRepository;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/admin/orders")
//@PreAuthorize("hasAuthority('MANAGE_ORDERS')")
//@RequiredArgsConstructor
//public class AdminOrderController extends BaseController {
//    private final AdminOrderService adminOrderService;
//    private final SecurityService securityService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/manual")
//    public ResponseEntity<BaseResponse<OrderResponse>> createManualOrder(
//            @Valid @RequestBody ManualOrderRequest request) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.createManualOrder(adminUserId, request);
//        return created(response, "Manual order created successfully");
//    }
//
//    @GetMapping
//    public ResponseEntity<BaseResponse<PaginatedResponse<OrderResponse>>> searchOrders(
//            @ModelAttribute OrderSearchCriteria criteria,
//            Pageable pageable) {
//        PaginatedResponse<OrderResponse> response = adminOrderService.searchOrders(criteria, pageable);
//        return paginated(response, "Orders retrieved successfully");
//    }
//
//    @GetMapping("/{orderId}")
//    public ResponseEntity<BaseResponse<OrderResponse>> getOrderWithHistory(@PathVariable Long orderId) {
//        OrderResponse response = adminOrderService.getOrderWithHistory(orderId);
//        return ok(response, "Order with history retrieved successfully");
//    }
//
//    @GetMapping("/status/{status}")
//    public ResponseEntity<BaseResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
//        List<OrderResponse> response = adminOrderService.getOrdersByStatus(status);
//        return ok(response, "Orders retrieved successfully");
//    }
//
//    @PatchMapping("/{orderId}/status")
//    public ResponseEntity<BaseResponse<OrderResponse>> updateOrderStatus(
//            @PathVariable Long orderId,
//            @RequestParam OrderStatus status,
//            @RequestParam(required = false) String notes) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.updateOrderStatus(orderId, status, adminUserId, notes);
//        return ok(response, "Order status updated successfully");
//    }
//
//    @PatchMapping("/{orderId}/payment-status")
//    public ResponseEntity<BaseResponse<OrderResponse>> updatePaymentStatus(
//            @PathVariable Long orderId,
//            @RequestParam PaymentStatus paymentStatus,
//            @RequestParam(required = false) String notes) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.updatePaymentStatus(orderId, paymentStatus, adminUserId, notes);
//        return ok(response, "Payment status updated successfully");
//    }
//
//    @PostMapping("/{orderId}/refund")
//    public ResponseEntity<BaseResponse<OrderResponse>> processRefund(
//            @PathVariable Long orderId,
//            @Valid @RequestBody RefundRequest request) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.processRefund(orderId, request, adminUserId);
//        return ok(response, "Refund processed successfully");
//    }
//
//    @PostMapping("/{orderId}/notes")
//    public ResponseEntity<BaseResponse<OrderResponse>> addOrderNote(
//            @PathVariable Long orderId,
//            @RequestParam String note) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.addOrderNote(orderId, note, adminUserId);
//        return ok(response, "Order note added successfully");
//    }
//
//    @PutMapping("/{orderId}/reassign")
//    public ResponseEntity<BaseResponse<OrderResponse>> reassignOrder(
//            @PathVariable Long orderId,
//            @RequestParam Long newVendorId) {
//        Long adminUserId = getCurrentUserId();
//        OrderResponse response = adminOrderService.reassignOrder(orderId, newVendorId, adminUserId);
//        return ok(response, "Order reassigned successfully");
//    }
//
//    @GetMapping("/stats")
//    public ResponseEntity<BaseResponse<OrderStatsResponse>> getOrderStats() {
//        OrderStatsResponse response = adminOrderService.getOrderStats();
//        return ok(response, "Order stats retrieved successfully");
//    }
//
//    // Fixed helper method to get current user ID for management users
//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new RuntimeException("User not authenticated");
//        }
//
//        String currentUsername = authentication.getName();
//        User user = userRepository.findByEmail(currentUsername)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // For admin endpoints, we expect management users (admin, superadmin, etc.)
//        // These users might not have customer profiles, so we return the user ID directly
//        return user.getId();
//    }
//}