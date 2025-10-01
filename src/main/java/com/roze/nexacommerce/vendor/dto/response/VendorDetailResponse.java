package com.roze.nexacommerce.vendor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailResponse {
    private VendorResponse vendor;
    private UserInfo userInfo;


}