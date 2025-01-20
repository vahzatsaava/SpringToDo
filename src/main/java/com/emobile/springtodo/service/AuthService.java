package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.UserRegistrationRequest;

public interface AuthService {
    AuthResponse register(UserRegistrationRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshAccessToken(String currentToken);
}
