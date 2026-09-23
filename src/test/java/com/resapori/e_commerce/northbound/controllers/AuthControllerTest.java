package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.auth.ForgotPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.ResetPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpResponse;
import com.resapori.e_commerce.service.IAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final IAuthService authService = Mockito.mock(IAuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    @DisplayName("forgotPassword should delegate to service and return 200 with confirmation")
    void shouldReturnOkForForgotPassword() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(authService.forgotPassword(request)).thenReturn(Map.of("message", "OTP has been sent to your email"));

        ResponseEntity<Map<String, String>> response = controller.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OTP has been sent to your email", response.getBody().get("message"));
        verify(authService).forgotPassword(request);
    }

    @Test
    @DisplayName("verifyOtp should delegate to service and return 200 with resetPasswordToken")
    void shouldReturnOkForVerifyOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest("test@example.com", "123456");
        VerifyOtpResponse mockResponse = VerifyOtpResponse.builder()
                .message("OTP verified successfully")
                .resetPasswordToken("dummy-reset-token")
                .build();
        when(authService.verifyOtp(request)).thenReturn(mockResponse);

        ResponseEntity<VerifyOtpResponse> response = controller.verifyOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("dummy-reset-token", response.getBody().getResetPasswordToken());
        verify(authService).verifyOtp(request);
    }

    @Test
    @DisplayName("resetPassword should delegate to service and return 200 with success message")
    void shouldReturnOkForResetPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("dummy-reset-token", "NewPassword123");
        when(authService.resetPassword(request)).thenReturn(Map.of("message", "Password has been reset successfully."));

        ResponseEntity<Map<String, String>> response = controller.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password has been reset successfully.", response.getBody().get("message"));
        verify(authService).resetPassword(request);
    }
}
