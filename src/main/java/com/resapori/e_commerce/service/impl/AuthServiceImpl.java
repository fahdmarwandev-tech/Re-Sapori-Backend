package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.security.CustomUserDetails;
import com.resapori.e_commerce.common.security.JwtService;
import com.resapori.e_commerce.northbound.dto.auth.AuthResponse;
import com.resapori.e_commerce.northbound.dto.auth.LoginRequest;
import com.resapori.e_commerce.northbound.dto.auth.RefreshTokenRequest;
import com.resapori.e_commerce.northbound.dto.auth.RegisterRequest;
import com.resapori.e_commerce.service.IAuthService;
import com.resapori.e_commerce.southbound.entity.RefreshToken;
import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.repository.IRefreshTokenRepository;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists with this email");
        }

        User user = new User();
        String[] nameParts = request.getName() != null ? request.getName().split(" ", 2) : new String[]{"", ""};
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
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
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        String jwtToken = jwtService.generateToken(userDetails);
        
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
        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
        tokenOpt.ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }
}
