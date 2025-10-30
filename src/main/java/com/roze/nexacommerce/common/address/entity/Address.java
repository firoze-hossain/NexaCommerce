package com.roze.nexacommerce.common.address.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType; // HOME, OFFICE

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "city", nullable = false)
    private String city = "Dhaka";

    @Column(name = "landmark")
    private String landmark;

    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = false;
}