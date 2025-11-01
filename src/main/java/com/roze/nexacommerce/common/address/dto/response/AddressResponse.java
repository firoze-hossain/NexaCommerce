package com.roze.nexacommerce.common.address.dto.response;

import com.roze.nexacommerce.common.address.enums.AddressZone;
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
    private String addressType;
    private String fullName;
    private String phone;
    private String area;
    private String addressLine;
    private String city;
    private String landmark;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AddressZone addressZone;
    private Boolean isInsideDhaka;
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(addressLine);
        if (area != null && !area.isEmpty()) {
            address.append(", ").append(area);
        }
        if (city != null && !city.isEmpty()) {
            address.append(", ").append(city);
        }
        if (landmark != null && !landmark.isEmpty()) {
            address.append(" (Near ").append(landmark).append(")");
        }
        return address.toString();
    }
}