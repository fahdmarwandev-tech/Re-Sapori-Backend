package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.common.security.CustomUserDetails;
import com.resapori.e_commerce.common.security.JwtService;
import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.ForgotPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;
import com.resapori.e_commerce.northbound.dto.auth.ResetPasswordRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpRequest;
import com.resapori.e_commerce.northbound.dto.auth.VerifyOtpResponse;
import com.resapori.e_commerce.service.IAuthService;
import com.resapori.e_commerce.service.IEmailService;
import com.resapori.e_commerce.southbound.entity.RefreshToken;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.entity.UserOtp;
import com.resapori.e_commerce.southbound.repository.IRefreshTokenRepository;
import com.resapori.e_commerce.southbound.repository.IRoleRepository;
import com.resapori.e_commerce.southbound.repository.IUserOtpRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 15;
    private static final String OTP_SUBJECT = "Re-Sapori - Password Reset OTP";

    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IRoleRepository roleRepository;
    private final IUserOtpRepository userOtpRepository;
    private final IEmailService emailService;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists with this email");
        }

        Role customerRole = resolveCustomerRole();
        User user = buildUser(request, customerRole);
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));

        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());

        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenOpt.ifPresent(refreshTokenRepository::delete);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        RefreshToken refreshToken = tokenOpt.get();
        String jwtToken = jwtService.generateToken(new CustomUserDetails(refreshToken.getUser()));

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Role resolveCustomerRole() {
        return roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "CUSTOMER role not seeded in database"));
    }

    private User buildUser(RegisterRequest request, Role customerRole) {
        String[] nameParts = request.getName() != null
                ? request.getName().split(" ", 2)
                : new String[]{"", ""};

        User user = new User();
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(customerRole));
        return user;
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Verify user is registered
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No registered account found with email: " + email));

        // 2. Invalidate any existing OTPs for this email
        userOtpRepository.deleteAllByEmail(email);

        // 3. Generate unique 6-digit OTP
        String otp = generateUniqueOtp();

        // 4. Save to database with 5-minute expiry
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        UserOtp userOtp = UserOtp.builder()
                .email(email)
                .otp(otp)
                .isVerified(false)
                .expiryDate(expiryTime)
                .build();
        userOtpRepository.save(userOtp);

        // 5. Dispatch email
        String body = "Hello,\n\nYour OTP code for password reset is: " + otp
                + "\n\nThis code will expire in " + OTP_EXPIRATION_MINUTES + " minutes."
                + "\n\nIf you did not request a password reset, please ignore this email.";
        emailService.sendEmail(email, OTP_SUBJECT, body);

        return Map.of("message", "OTP has been sent to your email");
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();

        // 1. Retrieve OTP record for this email
        UserOtp userOtp = userOtpRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No OTP request found for email: " + email + ". Please request a new OTP."));

        // 2. Check if already verified
        if (Boolean.TRUE.equals(userOtp.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This OTP has already been verified.");
        }

        // 3. Check if OTP is expired
        if (userOtp.getExpiryDate() == null || LocalDateTime.now().isAfter(userOtp.getExpiryDate())) {
            userOtpRepository.delete(userOtp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP code has expired. Please request a new OTP.");
        }

        // 4. Check OTP match
        if (!userOtp.getOtp().equals(inputOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or incorrect OTP code.");
        }

        // 5. Mark as verified and generate single-use reset password token (valid for 15 minutes)
        String resetToken = UUID.randomUUID().toString();
        userOtp.setIsVerified(true);
        userOtp.setResetToken(resetToken);
        userOtp.setResetTokenExpiry(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES));
        userOtpRepository.save(userOtp);

        return VerifyOtpResponse.builder()
                .message("OTP verified successfully")
                .resetPasswordToken(resetToken)
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        String newPassword = request.getNewPassword();

        // 1. Validate token existence
        UserOtp userOtp = userOtpRepository.findByResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or non-existent password reset token."));

        // 2. Validate OTP was verified
        if (!Boolean.TRUE.equals(userOtp.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP was not verified for this reset token.");
        }

        // 3. Check if reset token is expired
        if (userOtp.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(userOtp.getResetTokenExpiry())) {
            userOtpRepository.delete(userOtp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired. Please request a new OTP.");
        }

        // 4. Find user
        User user = userRepository.findByEmail(userOtp.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User associated with this token was not found."));

        // 5. Check that new password is NOT the same as old password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as your old password.");
        }

        // 6. Update user's password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 7. Invalidate reset token and terminate active refresh sessions
        userOtpRepository.delete(userOtp);
        refreshTokenRepository.deleteByUser(user);

        return Map.of("message", "Password has been reset successfully. You can now log in with your new password.");
    }

    private String generateUniqueOtp() {
        String otp = authUtil.generateOtp(6);
        if (userOtpRepository.existsByOtp(otp)) {
            return generateUniqueOtp();
        }
        return otp;
    }
}
