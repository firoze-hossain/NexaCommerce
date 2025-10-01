package com.roze.nexacommerce.vendor.dto.request;

import com.roze.nexacommerce.vendor.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegistrationRequest {
    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String contactPersonName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    private String companyName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid business email format")
    private String businessEmail;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    private String taxNumber;
    private String description;
    private String website;
    private String businessRegistrationNumber;
}