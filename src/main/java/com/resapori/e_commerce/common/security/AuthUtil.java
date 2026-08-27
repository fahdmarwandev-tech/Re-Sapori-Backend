package com.resapori.e_commerce.common.security;

import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final IUserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            // Fetch fresh from DB to avoid lazy loading issues on relationships, or just return the cached one
            // We'll return the cached one from userDetails, but it's often safer to re-fetch if you need updated state
            return userRepository.findById(customUserDetails.getUser().getId()).orElse(null);
        }

        return null;
    }
    
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUsername();
        }
        return null;
    }
}
