package com.roze.nexacommerce.order.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.order.enums.OrderAction;
import com.roze.nexacommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderAction action;

    private String description;
    private String oldValue;
    private String newValue;
    private String notes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderHistory)) return false;
        OrderHistory that = (OrderHistory) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}