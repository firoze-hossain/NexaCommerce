package com.roze.nexacommerce.vendor.dto.response;

import com.roze.nexacommerce.vendor.entity.VendorProfile;
import com.roze.nexacommerce.vendor.enums.BusinessType;
import com.roze.nexacommerce.vendor.enums.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
    private Long id;
    private String companyName;
    private String businessEmail;
    private String phone;
    private BusinessType businessType;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String website;
    private BigDecimal commissionRate;
    private VendorStatus status;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalSales;
    private Double ratingAvg;
    private Integer ratingCount;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}