package com.roze.nexacommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddress {
    @Column(name = "billing_full_name")
    private String fullName;

    @Column(name = "billing_phone")
    private String phone;

    @Column(name = "billing_address_line1")
    private String addressLine1;

    @Column(name = "billing_address_line2")
    private String addressLine2;

    @Column(name = "billing_city")
    private String city;

    @Column(name = "billing_state")
    private String state;

    @Column(name = "billing_postal_code")
    private String postalCode;

    @Column(name = "billing_country")
    private String country;

    @Builder.Default
    @Column(name = "billing_same_as_shipping")
    private Boolean sameAsShipping = true;
}