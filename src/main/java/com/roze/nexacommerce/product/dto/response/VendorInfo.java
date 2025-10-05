package com.roze.nexacommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorInfo {
    private Long vendorId;
    private String companyName;
    private String businessEmail;
    private String phone;
    private Double rating;
}