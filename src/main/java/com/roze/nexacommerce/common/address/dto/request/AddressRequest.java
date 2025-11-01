package com.roze.nexacommerce.common.address.dto.request;

import com.roze.nexacommerce.common.address.enums.AddressType;
import com.roze.nexacommerce.common.address.enums.AddressZone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Address type is required")
    private AddressType addressType;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "01[3-9]\\d{8}", message = "Please enter a valid Bangladeshi phone number")
    private String phone;

    @NotBlank(message = "Area is required")
    private String area;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String city;

    private String landmark;

    @Builder.Default
    @NotNull(message = "Is default flag is required")
    private Boolean isDefault = false;

    @NotNull(message = "Address zone is required")
    private AddressZone addressZone;

    private Boolean isInsideDhaka;
}