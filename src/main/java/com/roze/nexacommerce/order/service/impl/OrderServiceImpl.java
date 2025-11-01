package com.roze.nexacommerce.order.service.impl;

import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.common.address.entity.Address;
import com.roze.nexacommerce.common.address.enums.AddressZone;
import com.roze.nexacommerce.common.address.service.AddressService;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.customer.repository.CustomerProfileRepository;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.dto.request.GuestAddressRequest;
import com.roze.nexacommerce.order.dto.request.GuestOrderCreateRequest;
import com.roze.nexacommerce.order.dto.request.OrderCreateRequest;
import com.roze.nexacommerce.order.dto.request.OrderItemRequest;
import com.roze.nexacommerce.order.dto.response.OrderResponse;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderAddress;
import com.roze.nexacommerce.order.entity.OrderHistory;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.order.enums.OrderAction;
import com.roze.nexacommerce.order.enums.OrderSource;
import com.roze.nexacommerce.order.enums.OrderStatus;
import com.roze.nexacommerce.order.enums.PaymentStatus;
import com.roze.nexacommerce.order.mapper.OrderMapper;
import com.roze.nexacommerce.order.repository.OrderRepository;
import com.roze.nexacommerce.order.service.OrderService;
import com.roze.nexacommerce.product.entity.Product;
import com.roze.nexacommerce.product.repository.ProductRepository;
import com.roze.nexacommerce.security.SecurityService;
import com.roze.nexacommerce.vendor.entity.VendorProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerProfileRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final AddressService addressService;
    private final SecurityService securityService;
    @Value("${app.shipping.inside-dhaka:60}")
    private BigDecimal insideDhakaShipping;

    @Value("${app.shipping.outside-dhaka:120}")
    private BigDecimal outsideDhakaShipping;

    @Value("${app.shipping.free-shipping-threshold:1000}")
    private BigDecimal freeShippingThreshold;

    @Override
    @Transactional
    public OrderResponse createOrder(Long customerId, OrderCreateRequest request) {
        log.info("Creating order for customer ID: {}", customerId);
// Validate customer ownership first
        if (!securityService.isCurrentCustomer(customerId)) {
            throw new AccessDeniedException("Customer does not belong to current user");
        }
        // Validate address ownership
        validateAddressOwnership(customerId, request.getShippingAddressId());

        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
// Get current user ID from security context
        Long currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        // Get address and create order address
        Address address = addressService.getAddressEntityById(request.getShippingAddressId());
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Address does not belong to the current user");
        }
        OrderAddress orderAddress = OrderAddress.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .area(address.getArea())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .landmark(address.getLandmark())
                .build();

        // Process order items
        List<OrderItem> orderItems = processOrderItems(request.getItems());
        BigDecimal shippingAmount = calculateShippingAmount(address, calculateTotalAmount(orderItems));
        // Get vendor from first product
        VendorProfile vendor = orderItems.get(0).getProduct().getVendor();

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .vendor(vendor)
                .source(OrderSource.WEBSTORE)
                .totalAmount(calculateTotalAmount(orderItems))
                .shippingAmount(request.getShippingAmount())
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .couponCode(request.getCouponCode())
                .couponDiscount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(orderAddress)
                .billingAddress(orderAddress) // Same as shipping for simplicity
                .customerNotes(request.getCustomerNotes())
                .shippingAmount(shippingAmount)
                .build();

        order.calculateTotals();
        orderItems.forEach(order::addOrderItem);

        // Add order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.ORDER_CREATED)
                .description("Order created successfully")
                .build();
        order.addHistory(history);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} and number: {}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse createGuestOrder(GuestOrderCreateRequest request) {
        log.info("Creating guest order for email: {}", request.getGuestEmail());

        // Process order items
        List<OrderItem> orderItems = processOrderItems(request.getItems());

        // Get vendor from first product
        VendorProfile vendor = orderItems.get(0).getProduct().getVendor();

        // Create order address from guest address
        OrderAddress orderAddress = OrderAddress.builder()
                .fullName(request.getShippingAddress().getFullName())
                .phone(request.getShippingAddress().getPhone())
                .area(request.getShippingAddress().getArea())
                .addressLine(request.getShippingAddress().getAddressLine())
                .city(request.getShippingAddress().getCity() != null ?
                        request.getShippingAddress().getCity() : "Dhaka")
                .landmark(request.getShippingAddress().getLandmark())
                .build();
        BigDecimal shippingAmount = calculateGuestShippingAmount(
                request.getShippingAddress(),
                calculateTotalAmount(orderItems)
        );
        // Create guest order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(null) // No customer for guest orders
                .vendor(vendor)
                .source(OrderSource.WEBSTORE)
                .totalAmount(calculateTotalAmount(orderItems))
                .shippingAmount(request.getShippingAmount())
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .couponCode(request.getCouponCode())
                .couponDiscount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(orderAddress)
                .billingAddress(orderAddress)
                .customerNotes(request.getCustomerNotes())
                .guestEmail(request.getGuestEmail())
                .guestName(request.getGuestName())
                .guestPhone(request.getShippingAddress().getPhone())
                .build();

        order.calculateTotals();
        orderItems.forEach(order::addOrderItem);

        // Add order history
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action(OrderAction.ORDER_CREATED)
                .description("Guest order created successfully")
                .build();
        order.addHistory(history);

        Order savedOrder = orderRepository.save(order);
        log.info("Guest order created successfully with ID: {} and number: {}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        return orderMapper.toResponse(savedOrder);
    }

    private BigDecimal calculateShippingAmount(Address address, BigDecimal subtotal) {
        // Free shipping for orders above threshold
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate based on address zone
        if (address.getIsInsideDhaka() != null && address.getIsInsideDhaka()) {
            return insideDhakaShipping;
        } else if (address.getAddressZone() == AddressZone.OUTSIDE_DHAKA) {
            return outsideDhakaShipping;
        } else {
            // Default shipping for other zones
            return outsideDhakaShipping;
        }
    }

    private BigDecimal calculateGuestShippingAmount(GuestAddressRequest address, BigDecimal subtotal) {
        // Free shipping for orders above threshold
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        // Determine zone based on city/area for guest orders
        boolean isInsideDhaka = isInsideDhaka(address.getCity(), address.getArea());

        if (isInsideDhaka) {
            return insideDhakaShipping;
        } else {
            return outsideDhakaShipping;
        }
    }

    private boolean isInsideDhaka(String city, String area) {
        if (city != null && !city.equalsIgnoreCase("Dhaka")) {
            return false;
        }

        // List of areas considered inside Dhaka
        List<String> insideDhakaAreas = Arrays.asList(
                "Gulshan", "Banani", "Baridhara", "Bashundhara", "Uttara",
                "Dhanmondi", "Mirpur", "Mohammadpur", "Motijheel", "Malibagh",
                "Rampura", "Badda", "Mohakhali", "Farmgate", "Shyamoli"
        );

        return area != null && insideDhakaAreas.stream()
                .anyMatch(dhakaArea -> dhakaArea.equalsIgnoreCase(area));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getGuestOrder(String orderNumber, String email) {
        log.info("Fetching guest order with number: {} and email: {}", orderNumber, email);

        Order order = orderRepository.findByOrderNumberAndGuestEmail(orderNumber, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.debug("Fetching order by ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        log.debug("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {
        log.debug("Fetching order ID: {} for customer ID: {}", orderId, customerId);

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
        log.debug("Fetching orders for customer ID: {}", customerId);

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
        log.debug("Fetching recent {} orders for customer ID: {}", limit, customerId);

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
        log.info("Updating order ID: {} status to: {}", orderId, status);

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
        log.info("Updating order ID: {} payment status to: {}", orderId, paymentStatus);

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
        log.info("Cancelling order ID: {} for customer ID: {}", orderId, customerId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // For guest orders, we don't check customer ownership
        if (order.getCustomer() != null && !order.getCustomer().getId().equals(customerId)) {
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
                .description("Order cancelled by " + (order.isGuestOrder() ? "guest" : "customer"))
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
        log.info("Adding note to order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        String currentNotes = order.getInternalNotes();
        String newNotes = currentNotes != null ? currentNotes + "\n" + note : note;
        order.setInternalNotes(newNotes);

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

    // ============ HELPER METHODS ============

    private List<OrderItem> processOrderItems(List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            if (!product.isAvailable()) {
                throw new IllegalStateException("Product is not available: " + product.getName());
            }

            if (product.getTrackQuantity() && product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity());
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

    private void validateAddressOwnership(Long customerId, Long addressId) {
        CustomerProfile customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Long userId = customer.getUser().getId();

        Address address = addressService.getAddressEntityById(addressId);
        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Address does not belong to the current user");
        }
    }
}