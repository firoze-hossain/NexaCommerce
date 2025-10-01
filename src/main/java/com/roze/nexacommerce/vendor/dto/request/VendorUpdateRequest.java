package com.roze.nexacommerce.vendor.dto.request;

import com.roze.nexacommerce.vendor.enums.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorUpdateRequest {
    private String companyName;
    private String businessEmail;
    private String phone;
    private String description;
    private String website;
    private String taxNumber;
    private BigDecimal commissionRate;
    private VendorStatus status;
}