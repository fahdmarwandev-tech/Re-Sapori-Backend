package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
}
