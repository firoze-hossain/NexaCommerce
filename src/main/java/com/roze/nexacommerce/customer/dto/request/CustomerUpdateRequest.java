package com.roze.nexacommerce.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    private String phone;
    private String profileImage;
    private LocalDate dateOfBirth;
    private String currency;
    private String language;
    private Boolean newsletterSubscribed;
}