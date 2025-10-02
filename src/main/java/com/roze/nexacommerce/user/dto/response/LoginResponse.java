package com.roze.nexacommerce.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private Long expiresIn;
    private UserResponse user;
}
