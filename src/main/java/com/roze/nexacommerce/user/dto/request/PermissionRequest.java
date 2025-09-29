package com.roze.nexacommerce.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionRequest {
    @NotBlank(message = "Permission name is required")
    @Size(min = 2, max = 50, message = "Permission name must be between 2 and 50 characters")
    private String name;
    private String description;
}
