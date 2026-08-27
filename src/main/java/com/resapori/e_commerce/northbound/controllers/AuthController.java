package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.common.security.CustomUserDetails;
import com.resapori.e_commerce.common.security.JwtService;
import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;
import com.resapori.e_commerce.southbound.entity.RefreshToken;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.repository.IRefreshTokenRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Note: You should ideally assign a default Role here by fetching it from IRoleRepository.
        
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = createRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        String jwtToken = jwtService.generateToken(userDetails);
        
        // Revoke old refresh tokens for this user and create a new one
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
        
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            if (tokenOpt.isPresent()) {
                refreshTokenRepository.delete(tokenOpt.get());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken refreshToken = tokenOpt.get();
        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }
}
