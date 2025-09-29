package com.roze.nexacommerce.user.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.user.dto.request.LoginRequest;
import com.roze.nexacommerce.user.dto.response.LoginResponse;
import com.roze.nexacommerce.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
