package com.roze.nexacommerce.user.service;

import com.roze.nexacommerce.user.dto.request.LoginRequest;
import com.roze.nexacommerce.user.dto.response.LoginResponse;

public interface AuthenticationService {
     LoginResponse login(LoginRequest request);

}
