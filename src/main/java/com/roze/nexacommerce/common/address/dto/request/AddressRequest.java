package com.roze.nexacommerce.common.address.dto.request;


import com.roze.nexacommerce.common.address.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    private String landmark;
    
    @NotNull(message = "Is default flag is required")
    private Boolean isDefault;

    private String contactName;
    private String contactPhone;
    private String companyName;
}