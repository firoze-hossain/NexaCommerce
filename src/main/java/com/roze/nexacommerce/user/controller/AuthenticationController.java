package com.roze.nexacommerce.user.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.user.dto.request.LoginRequest;
import com.roze.nexacommerce.user.dto.response.LoginResponse;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import com.roze.nexacommerce.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ok(response, "Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authenticationService.refreshToken(refreshToken);
        return ok(response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return noContent("Logout successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponse userResponse = authenticationService.getCurrentUser(email);
        return ok(userResponse, "User retrieved successfully");
    }
}
