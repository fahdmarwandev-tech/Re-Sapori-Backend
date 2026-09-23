package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.ForgotPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;
import com.resapori.e_commerce.northbound.dto.auth.ResetPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpResponse;

import java.util.Map;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);

    Map<String, String> forgotPassword(ForgotPasswordRequest request);
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
    Map<String, String> resetPassword(ResetPasswordRequest request);
}
