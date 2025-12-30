// ReturnRequest.java
package com.roze.nexacommerce.returns.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.returns.enums.ReturnReason;
import com.roze.nexacommerce.returns.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String returnNumber; // RMA-2024-001234
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerProfile customer;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnReason reason;
    
    private String reasonDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "restocking_fee", precision = 10, scale = 2)
    private BigDecimal restockingFee;
    
    @Column(name = "return_shipping_cost", precision = 10, scale = 2)
    private BigDecimal returnShippingCost;
    
    @Column(name = "pickup_required")
    @Builder.Default
    private Boolean pickupRequired = false;
    
    @Column(name = "pickup_scheduled_at")
    private LocalDateTime pickupScheduledAt;
    
    @Column(name = "return_label_url")
    private String returnLabelUrl;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "carrier")
    private String carrier;
    
    @Column(name = "refund_method")
    private String refundMethod; // original_payment, store_credit, bank_transfer
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReturnItem> returnItems = new ArrayList<>();
    
    // Helper methods
    public boolean isWithinReturnWindow() {
        return LocalDateTime.now().isBefore(
            order.getCreatedAt().plusDays(30)
        );
    }
    
    public boolean isEligibleForFreeReturn() {
        return order.getFinalAmount().compareTo(BigDecimal.valueOf(50)) >= 0;
    }
    
    public void calculateRefund() {
        BigDecimal itemsTotal = returnItems.stream()
            .map(ReturnItem::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = itemsTotal;
        
        // Apply restocking fee if any
        if (restockingFee != null) {
            itemsTotal = itemsTotal.subtract(restockingFee);
        }
        
        // Deduct return shipping if customer pays
        if (returnShippingCost != null && returnShippingCost.compareTo(BigDecimal.ZERO) > 0) {
            itemsTotal = itemsTotal.subtract(returnShippingCost);
        }
        
        this.refundAmount = itemsTotal.max(BigDecimal.ZERO);
    }
}

