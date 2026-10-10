package com.resapori.e_commerce.service;

import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.common.security.JwtService;
import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.ForgotPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;
import com.resapori.e_commerce.northbound.dto.auth.ResetPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpResponse;
import com.resapori.e_commerce.service.impl.AuthServiceImpl;
import com.resapori.e_commerce.southbound.entity.RefreshToken;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.entity.UserOtp;
import com.resapori.e_commerce.southbound.repository.IRefreshTokenRepository;
import com.resapori.e_commerce.southbound.repository.IRoleRepository;
import com.resapori.e_commerce.southbound.repository.IUserOtpRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceOtpTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IUserOtpRepository userOtpRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private IEmailTemplateService emailTemplateService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    // ==========================================
    // 1. forgotPassword Tests
    // ==========================================

    @Test
    @DisplayName("forgotPassword: Should throw 404 when email is not registered")
    void forgotPassword_shouldThrowWhenEmailNotRegistered() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.forgotPassword(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No registered account found"));
        verify(userOtpRepository, never()).save(any());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }

    @Test
    @DisplayName("forgotPassword: Should save OTP and send email when email exists")
    void forgotPassword_shouldSaveOtpAndSendEmail() {
        String email = "registered@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authUtil.generateOtp(6)).thenReturn("123456");
        when(userOtpRepository.existsByOtp("123456")).thenReturn(false);
        when(emailTemplateService.buildOtpEmail(eq("123456"), eq(5))).thenReturn("<html>123456</html>");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        Map<String, String> response = authService.forgotPassword(request);

        assertEquals("OTP has been sent to your email", response.get("message"));
        verify(userOtpRepository).deleteAllByEmail(email);

        ArgumentCaptor<UserOtp> otpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        verify(userOtpRepository).save(otpCaptor.capture());
        UserOtp savedOtp = otpCaptor.getValue();
        assertEquals(email, savedOtp.getEmail());
        assertEquals("123456", savedOtp.getOtp());
        assertFalse(savedOtp.getIsVerified());
        assertNotNull(savedOtp.getExpiryDate());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendHtmlEmail(eq(email), eq("Re-Sapori - Password Reset OTP"), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("123456"));
    }

    // ==========================================
    // 2. verifyOtp Tests
    // ==========================================

    @Test
    @DisplayName("verifyOtp: Should throw 400 when no OTP request exists for email")
    void verifyOtp_shouldThrowWhenNoOtpExists() {
        String email = "test@example.com";
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.empty());

        VerifyOtpRequest request = new VerifyOtpRequest(email, "123456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No OTP request found"));
    }

    @Test
    @DisplayName("verifyOtp: Should throw 400 when OTP is already verified")
    void verifyOtp_shouldThrowWhenAlreadyVerified() {
        String email = "test@example.com";
        UserOtp otp = UserOtp.builder().email(email).otp("123456").isVerified(true).build();
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(otp));

        VerifyOtpRequest request = new VerifyOtpRequest(email, "123456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already been verified"));
    }

    @Test
    @DisplayName("verifyOtp: Should throw 400 and delete expired OTP when expired")
    void verifyOtp_shouldThrowWhenExpired() {
        String email = "test@example.com";
        UserOtp otp = UserOtp.builder()
                .email(email)
                .otp("123456")
                .isVerified(false)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(otp));

        VerifyOtpRequest request = new VerifyOtpRequest(email, "123456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("expired"));
        verify(userOtpRepository).delete(otp);
    }

    @Test
    @DisplayName("verifyOtp: Should throw 400 when OTP code is incorrect")
    void verifyOtp_shouldThrowWhenOtpIncorrect() {
        String email = "test@example.com";
        UserOtp otp = UserOtp.builder()
                .email(email)
                .otp("123456")
                .isVerified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(otp));

        VerifyOtpRequest request = new VerifyOtpRequest(email, "999999");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid or incorrect OTP"));
    }

    @Test
    @DisplayName("verifyOtp: Should return resetPasswordToken when OTP is valid")
    void verifyOtp_shouldReturnTokenWhenValid() {
        String email = "test@example.com";
        UserOtp otp = UserOtp.builder()
                .email(email)
                .otp("123456")
                .isVerified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(otp));

        VerifyOtpRequest request = new VerifyOtpRequest(email, "123456");
        VerifyOtpResponse response = authService.verifyOtp(request);

        assertNotNull(response.getResetPasswordToken());
        assertEquals("OTP verified successfully", response.getMessage());
        assertTrue(otp.getIsVerified());
        assertNotNull(otp.getResetToken());
        assertNotNull(otp.getResetTokenExpiry());
        verify(userOtpRepository).save(otp);
    }

    // ==========================================
    // 3. resetPassword Tests
    // ==========================================

    @Test
    @DisplayName("resetPassword: Should throw 400 when token is invalid or not found")
    void resetPassword_shouldThrowWhenTokenInvalid() {
        when(userOtpRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest("bad-token", "NewPass123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid or non-existent"));
    }

    @Test
    @DisplayName("resetPassword: Should throw 400 and delete token when reset token expired")
    void resetPassword_shouldThrowWhenTokenExpired() {
        String token = "expired-token";
        UserOtp otp = UserOtp.builder()
                .email("test@example.com")
                .resetToken(token)
                .isVerified(true)
                .resetTokenExpiry(LocalDateTime.now().minusMinutes(1))
                .build();
        when(userOtpRepository.findByResetToken(token)).thenReturn(Optional.of(otp));

        ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPass123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("token has expired"));
        verify(userOtpRepository).delete(otp);
    }

    @Test
    @DisplayName("resetPassword: Should throw 400 when new password is the same as the old password")
    void resetPassword_shouldThrowWhenPasswordIsSameAsOld() {
        String token = "valid-token";
        UserOtp otp = UserOtp.builder()
                .email("test@example.com")
                .resetToken(token)
                .isVerified(true)
                .resetTokenExpiry(LocalDateTime.now().plusMinutes(10))
                .build();
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_old_password");

        when(userOtpRepository.findByResetToken(token)).thenReturn(Optional.of(otp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", "hashed_old_password")).thenReturn(true);

        ResetPasswordRequest request = new ResetPasswordRequest(token, "OldPassword123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("same as your old password"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: Should update password, delete token, and revoke refresh tokens on success")
    void resetPassword_shouldUpdatePasswordSuccessfully() {
        String token = "valid-token";
        UserOtp otp = UserOtp.builder()
                .email("test@example.com")
                .resetToken(token)
                .isVerified(true)
                .resetTokenExpiry(LocalDateTime.now().plusMinutes(10))
                .build();
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_old_password");

        when(userOtpRepository.findByResetToken(token)).thenReturn(Optional.of(otp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("BrandNewPassword123", "hashed_old_password")).thenReturn(false);
        when(passwordEncoder.encode("BrandNewPassword123")).thenReturn("hashed_new_password");

        ResetPasswordRequest request = new ResetPasswordRequest(token, "BrandNewPassword123");
        Map<String, String> response = authService.resetPassword(request);

        assertTrue(response.get("message").contains("Password has been reset successfully"));
        assertEquals("hashed_new_password", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(userOtpRepository).delete(otp);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    // ==========================================
    // 4. Email Normalization Tests (Trim & Lowercase)
    // ==========================================

    @Test
    @DisplayName("register: Should trim and lowercase email when saving new user")
    void register_shouldTrimAndLowercaseEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("  John.Doe@Example.COM  ")
                .password("SecurePass123!")
                .build();

        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(jwtService.generateToken(any())).thenReturn("dummy_jwt");
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse res = authService.register(request);

        assertNotNull(res);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john.doe@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    @DisplayName("register: Should save phone number when provided in RegisterRequest")
    void register_shouldSavePhoneNumberWhenProvided() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Sarah Connor")
                .email("sarah@example.com")
                .password("SecurePass123!")
                .phoneNumber("01012345678")
                .build();

        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");

        when(userRepository.findByEmail("sarah@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("sarah@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(jwtService.generateToken(any())).thenReturn("dummy_jwt");
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse res = authService.register(request);

        assertNotNull(res);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("01012345678", userCaptor.getValue().getPhoneNumber());
        assertEquals("Sarah", userCaptor.getValue().getFirstName());
        assertEquals("Connor", userCaptor.getValue().getLastName());
    }

    @Test
    @DisplayName("login: Should trim and lowercase email during authentication")
    void login_shouldTrimAndLowercaseEmail() {
        LoginRequest request = LoginRequest.builder()
                .email("  User.Test@Example.COM  ")
                .password("Password123")
                .build();

        User user = new User();
        user.setEmail("user.test@example.com");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail("user.test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("dummy_jwt");
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse res = authService.login(request);

        assertNotNull(res);
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertEquals("user.test@example.com", tokenCaptor.getValue().getPrincipal());
    }

    @Test
    @DisplayName("forgotPassword: Should trim and lowercase email for user and OTP records")
    void forgotPassword_shouldTrimAndLowercaseEmail() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("  Cust.Support@DOMAIN.COM  ")
                .build();

        User user = new User();
        user.setEmail("cust.support@domain.com");

        when(userRepository.findByEmail("cust.support@domain.com")).thenReturn(Optional.of(user));
        when(authUtil.generateOtp(6)).thenReturn("112233");
        when(userOtpRepository.existsByOtp("112233")).thenReturn(false);
        when(emailTemplateService.buildOtpEmail(anyString(), anyInt())).thenReturn("<html>OTP</html>");

        Map<String, String> response = authService.forgotPassword(request);

        assertEquals("OTP has been sent to your email", response.get("message"));
        ArgumentCaptor<UserOtp> otpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        verify(userOtpRepository).save(otpCaptor.capture());
        assertEquals("cust.support@domain.com", otpCaptor.getValue().getEmail());
        verify(emailService).sendHtmlEmail(eq("cust.support@domain.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("verifyOtp: Should trim and lowercase email when querying UserOtp repository")
    void verifyOtp_shouldTrimAndLowercaseEmail() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("  Cust.Support@DOMAIN.COM  ")
                .otp("  654321  ")
                .build();

        UserOtp otp = UserOtp.builder()
                .email("cust.support@domain.com")
                .otp("654321")
                .isVerified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userOtpRepository.findByEmail("cust.support@domain.com")).thenReturn(Optional.of(otp));

        VerifyOtpResponse response = authService.verifyOtp(request);

        assertEquals("OTP verified successfully", response.getMessage());
        assertNotNull(response.getResetPasswordToken());
        verify(userOtpRepository).findByEmail("cust.support@domain.com");
    }

    // ==========================================
    // 5. Refresh Token & Session Longevity Tests
    // ==========================================

    @Test
    @DisplayName("refresh: Should implement sliding expiration and extend validity by 14 days")
    void refresh_shouldImplementSlidingExpiration() {
        String tokenString = "valid-refresh-token-uuid";
        User user = new User();
        user.setEmail("admin@resapori.com");
        user.setPasswordHash("hashed");

        RefreshToken existingToken = new RefreshToken();
        existingToken.setUser(user);
        existingToken.setToken(tokenString);
        existingToken.setExpiryDate(LocalDateTime.now().plusDays(1)); // 1 day remaining

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(existingToken));
        when(jwtService.generateToken(any())).thenReturn("new-jwt-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refresh(new RefreshTokenRequest(tokenString));

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getAccessToken());
        assertEquals(tokenString, response.getRefreshToken());

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();
        // Verifies sliding expiration extends past 10 days
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now().plusDays(10)));
    }

    @Test
    @DisplayName("refresh: Should throw 401 UNAUTHORIZED when refresh token is expired")
    void refresh_shouldThrow401WhenTokenExpired() {
        String tokenString = "expired-token";
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(tokenString);
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expiredToken));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refresh(new RefreshTokenRequest(tokenString)));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("login: Should purge expired tokens and preserve multi-device sessions")
    void login_shouldPurgeExpiredTokensAndPreserveOtherSessions() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@resapori.com")
                .password("Password123")
                .build();

        User user = new User();
        user.setEmail("admin@resapori.com");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail("admin@resapori.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("dummy_jwt");
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse res = authService.login(request);

        assertNotNull(res);
        // Ensure expired tokens are purged
        verify(refreshTokenRepository).deleteByExpiryDateBefore(any(LocalDateTime.class));
        // Ensure active sessions are NOT wiped with deleteByUser(user)
        verify(refreshTokenRepository, never()).deleteByUser(user);
    }
}
