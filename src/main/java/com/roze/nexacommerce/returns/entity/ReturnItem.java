// ReturnItem.java
package com.roze.nexacommerce.returns.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "return_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "condition_received")
    private String conditionReceived; // NEW, LIKE_NEW, DAMAGED
    
    private String notes;
}