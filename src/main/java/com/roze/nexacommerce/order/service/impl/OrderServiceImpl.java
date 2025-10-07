package com.roze.nexacommerce.order.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.dto.request.OrderCreateRequest;
import com.roze.nexacommerce.order.dto.request.OrderItemRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.entity.*;
import com.roze.nexacommerce.order.enums.OrderAction;
import com.roze.nexacommerce.order.enums.OrderSource;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import com.roze.nexacommerce.order.mapper.OrderMapper;
import com.roze.nexacommerce.order.repository.OrderHistoryRepository;
import com.roze.nexacommerce.order.repository.OrderItemRepository;
import com.roze.nexacommerce.order.repository.OrderRepository;
import com.roze.nexacommerce.order.service.OrderAddressService;
import com.roze.nexacommerce.order.service.OrderService;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.user.repository.UserRepository;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CustomerProfileRepository customerRepository;
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderAddressService orderAddressService;
    private final AddressService addressService;

    @Override
    @Transactional
    public OrderResponse createOrder(Long customerId, OrderCreateRequest request) {
        // Validate address ownership at the beginning
        validateAddressOwnership(customerId, request.getShippingAddressId());
        if (!request.shouldUseShippingAsBilling()) {
            validateAddressOwnership(customerId, request.getBillingAddressId());
        }
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        // Validate addresses and create embedded addresses
        ShippingAddress shippingAddress = orderAddressService.createShippingAddress(request.getShippingAddressId());

        BillingAddress billingAddress;
        if (request.shouldUseShippingAsBilling()) {
            billingAddress = orderAddressService.createBillingAddressFromShipping(shippingAddress);
        } else {
            billingAddress = orderAddressService.createBillingAddress(request.getBillingAddressId());
        }
        // Validate and process order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
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
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        // Get vendor from first product (assuming single vendor per order for simplicity)
        VendorProfile vendor = orderItems.get(0).getProduct().getVendor();

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .vendor(vendor)
                .source(OrderSource.WEBSTORE)
                .totalAmount(totalAmount)
                .shippingAmount(request.getShippingAmount())
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .couponCode(request.getCouponCode())
                .couponDiscount(BigDecimal.ZERO) // Calculate based on coupon logic
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(shippingAddress)  // ← ADD THIS LINE
                .billingAddress(billingAddress)    // ← ADD THIS LINE
                .customerNotes(request.getCustomerNotes())
                .build();

        // Calculate final amount
        order.calculateTotals();

        // Add order items
        orderItems.forEach(order::addOrderItem);

        // Add order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.ORDER_CREATED)
                .description("Order created successfully")
                .build();
        order.addHistory(history);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);

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
    public List<OrderResponse> getRecentOrders(Long customerId, int limit) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Pageable pageable = Pageable.ofSize(limit);
        Page<Order> orderPage = orderRepository.findByCustomer(customer, pageable);

        return orderPage.getContent().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.STATUS_CHANGED)
                .description("Order status updated")
                .oldValue(oldStatus.name())
                .newValue(status.name())
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated from {} to {} for order ID: {}", oldStatus, status, orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        PaymentStatus oldStatus = order.getPaymentStatus();
        order.setPaymentStatus(paymentStatus);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.PAYMENT_STATUS_CHANGED)
                .description("Payment status updated")
                .oldValue(oldStatus.name())
                .newValue(paymentStatus.name())
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Payment status updated from {} to {} for order ID: {}", oldStatus, paymentStatus, orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getTrackQuantity()) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.STATUS_CHANGED)
                .description("Order cancelled by customer")
                .oldValue(order.getStatus().name())
                .newValue(OrderStatus.CANCELLED.name())
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled successfully for order ID: {}", orderId);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse addOrderNote(Long orderId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setInternalNotes(note);

        // Add history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.NOTE_ADDED)
                .description("Internal note added")
                .notes(note)
                .build();
        order.addHistory(history);

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    private void validateAddressOwnership(Long customerId, Long addressId) {
        // Get the user ID from customer profile
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Long userId = customer.getUser().getId();

        Address address = addressService.getAddressEntityById(addressId);
        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Address does not belong to the current user");
        }
    }
}