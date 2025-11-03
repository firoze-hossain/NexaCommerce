package com.roze.nexacommerce.order.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.dto.request.ManualOrderRequest;
import com.roze.nexacommerce.order.dto.request.OrderItemRequest;
import com.roze.nexacommerce.order.dto.request.OrderSearchCriteria;
import com.roze.nexacommerce.order.dto.request.RefundRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.dto.response.OrderStatsResponse;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderAddress;
import com.roze.nexacommerce.order.entity.OrderHistory;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.order.enums.OrderAction;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import com.roze.nexacommerce.order.mapper.OrderMapper;
import com.roze.nexacommerce.order.repository.OrderRepository;
import com.roze.nexacommerce.order.service.AdminOrderService;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {
    private final OrderRepository orderRepository;
    private final CustomerProfileRepository customerRepository;
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final AddressService addressService;

    @Override
    @Transactional
    public OrderResponse createManualOrder(Long adminUserId, ManualOrderRequest request) {
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        CustomerProfile customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // For manual orders, validate that addresses belong to the customer
        validateAddressOwnershipForCustomer(request.getCustomerId(), request.getShippingAddressId());
        if (request.getBillingAddressId() != null && !request.getUseShippingAsBilling()) {
            validateAddressOwnershipForCustomer(request.getCustomerId(), request.getBillingAddressId());
        }

        // Create embedded addresses from common addresses
        OrderAddress shippingAddress = createOrderAddress(request.getShippingAddressId());

        OrderAddress billingAddress;
        if (request.getUseShippingAsBilling() != null && request.getUseShippingAsBilling()) {
            billingAddress = shippingAddress; // Use same address for billing
        } else if (request.getBillingAddressId() != null) {
            billingAddress = createOrderAddress(request.getBillingAddressId());
        } else {
            billingAddress = shippingAddress; // Default to shipping address
        }

        // Validate and process order items
        List<OrderItem> orderItems = processOrderItems(request.getItems());
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        // Get vendor from first product
        VendorProfile vendor = orderItems.get(0).getProduct().getVendor();

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .vendor(vendor)
                .processedBy(adminUser)
                .source(request.getSource())
                .totalAmount(totalAmount)
                .shippingAmount(request.getShippingAmount())
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .couponCode(request.getCouponCode())
                .couponDiscount(BigDecimal.ZERO)
                .status(OrderStatus.CONFIRMED) // Manual orders are typically confirmed immediately
                .paymentStatus(PaymentStatus.PAID) // Assuming manual orders are marked as paid
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .customerNotes(request.getCustomerNotes())
                .internalNotes(request.getInternalNotes())
                .build();

        // Calculate final amount
        order.calculateTotals();

        // Add order items
        orderItems.forEach(order::addOrderItem);

        // Add order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.ORDER_CREATED)
                .description("Manual order created by admin")
                .build();
        order.addHistory(history);

        Order savedOrder = orderRepository.save(order);
        log.info("Manual order created by admin {} with ID: {}", adminUser.getName(), savedOrder.getId());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Long adminUserId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.STATUS_CHANGED)
                .description("Order status updated by admin" + (notes != null ? ": " + notes : ""))
                .oldValue(oldStatus.name())
                .newValue(status.name())
                .notes(notes)
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated by admin {} from {} to {} for order ID: {}",
                adminUser.getName(), oldStatus, status, orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus, Long adminUserId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        PaymentStatus oldStatus = order.getPaymentStatus();
        order.setPaymentStatus(paymentStatus);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.PAYMENT_STATUS_CHANGED)
                .description("Payment status updated by admin" + (notes != null ? ": " + notes : ""))
                .oldValue(oldStatus.name())
                .newValue(paymentStatus.name())
                .notes(notes)
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Payment status updated by admin {} from {} to {} for order ID: {}",
                adminUser.getName(), oldStatus, paymentStatus, orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse processRefund(Long orderId, RefundRequest request, Long adminUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot refund order that is not paid");
        }

        // Update payment status
        PaymentStatus newPaymentStatus = request.getAmount().compareTo(order.getFinalAmount()) == 0 ?
                PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;

        order.setPaymentStatus(newPaymentStatus);

        // Restore product stock if full refund
        if (newPaymentStatus == PaymentStatus.REFUNDED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product.getTrackQuantity()) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.REFUND_PROCESSED)
                .description(String.format("Refund processed: %s - Reason: %s",
                        request.getAmount(), request.getReason()))
                .oldValue(PaymentStatus.PAID.name())
                .newValue(newPaymentStatus.name())
                .notes(request.getReason())
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Refund processed by admin {} for order ID: {}, amount: {}",
                adminUser.getName(), orderId, request.getAmount());

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse addOrderNote(Long orderId, String note, Long adminUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        String currentNotes = order.getInternalNotes();
        String newNotes = currentNotes != null ? currentNotes + "\n" + note : note;
        order.setInternalNotes(newNotes);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.NOTE_ADDED)
                .description("Internal note added by admin")
                .notes(note)
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse reassignOrder(Long orderId, Long newVendorId, Long adminUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        VendorProfile newVendor = vendorRepository.findById(newVendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", newVendorId));

        VendorProfile oldVendor = order.getVendor();
        order.setVendor(newVendor);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .performedBy(adminUser)
                .action(OrderAction.STATUS_CHANGED)
                .description("Order reassigned to different vendor")
                .oldValue(oldVendor != null ? oldVendor.getCompanyName() : "None")
                .newValue(newVendor.getCompanyName())
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order reassigned by admin {} from vendor {} to {} for order ID: {}",
                adminUser.getName(),
                oldVendor != null ? oldVendor.getCompanyName() : "None",
                newVendor.getCompanyName(),
                orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> searchOrders(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<Order> spec = buildSearchSpecification(criteria);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(orderMapper::toResponse)
                .toList();

        return PaginatedResponse.<OrderResponse>builder()
                .items(orderResponses)
                .totalItems(orderPage.getTotalElements())
                .currentPage(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderWithHistory(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // The history is already eagerly loaded in the entity, so it will be included
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats() {
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long completedOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Calculate revenue for current month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusSeconds(1);
        BigDecimal monthlyRevenue = orderRepository.getRevenueBetweenDates(startOfMonth, endOfMonth);

        // Calculate total revenue
        BigDecimal totalRevenue = orderRepository.getRevenueBetweenDates(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.now()
        );

        // Get orders by status
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            Long count = orderRepository.countByStatus(status);
            ordersByStatus.put(status.name(), count);
        }

        // Simplified revenue by month (you would need a more complex query for this)
        Map<String, BigDecimal> revenueByMonth = new HashMap<>();
        revenueByMonth.put("current", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        return OrderStatsResponse.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .ordersByStatus(ordersByStatus)
                .revenueByMonth(revenueByMonth)
                .build();
    }

    // ============ HELPER METHODS ============

    private Specification<Order> buildSearchSpecification(OrderSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), criteria.getCustomerId()));
            }

            if (criteria.getVendorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vendor").get("id"), criteria.getVendorId()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getPaymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), criteria.getPaymentStatus()));
            }

            if (criteria.getOrderNumber() != null && !criteria.getOrderNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("orderNumber"), "%" + criteria.getOrderNumber() + "%"));
            }

            if (criteria.getCustomerName() != null && !criteria.getCustomerName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("customer").get("user").get("name"),
                        "%" + criteria.getCustomerName() + "%"
                ));
            }

            if (criteria.getCustomerEmail() != null && !criteria.getCustomerEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("customer").get("user").get("email"),
                        "%" + criteria.getCustomerEmail() + "%"
                ));
            }

            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getStartDate().atStartOfDay()
                ));
            }

            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getEndDate().atTime(23, 59, 59)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<OrderItem> processOrderItems(List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            if (!product.isAvailable()) {
                throw new IllegalStateException("Product is not available: " + product.getName());
            }

            if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                    product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            // Update product stock
            if (product.getTrackQuantity()) {
                product.setStock(product.getStock() - itemRequest.getQuantity());
                productRepository.save(product);
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .compareAtPrice(product.getCompareAtPrice())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productImage(!product.getImages().isEmpty() ? product.getImages().get(0).getImageUrl() : null)
                    .build();

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    private OrderAddress createOrderAddress(Long addressId) {
        Address commonAddress = addressService.getAddressEntityById(addressId);

        return OrderAddress.builder()
                .fullName(commonAddress.getFullName())
                .phone(commonAddress.getPhone())
                .area(commonAddress.getArea())
                .addressLine(commonAddress.getAddressLine())
                .city(commonAddress.getCity())
                .landmark(commonAddress.getLandmark())
                .build();
    }

    private void validateAddressOwnershipForCustomer(Long customerId, Long addressId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Long userId = customer.getUser().getId();

        Address address = addressService.getAddressEntityById(addressId);
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the specified customer");
        }
    }
}