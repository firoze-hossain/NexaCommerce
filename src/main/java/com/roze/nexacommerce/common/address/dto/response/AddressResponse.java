package com.roze.nexacommerce.common.address.dto.response;

import com.roze.nexacommerce.common.address.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private AddressType addressType;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String landmark;
    private Boolean isDefault;
    private String contactName;
    private String contactPhone;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}