package com.pfe.management.service;

import com.pfe.management.dto.request.LoginRequest;
import com.pfe.management.dto.request.RegisterRequest;
import com.pfe.management.dto.response.AuthResponse;
import com.pfe.management.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
}

