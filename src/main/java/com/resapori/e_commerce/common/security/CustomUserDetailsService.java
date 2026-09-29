package com.resapori.e_commerce.common.security;

import com.resapori.e_commerce.southbound.entity.User;
import com.resapori.e_commerce.southbound.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim().toLowerCase() : null;
        User user = userRepository.findByEmail(cleanUsername)
                .or(() -> userRepository.findByEmailIgnoreCase(cleanUsername))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + cleanUsername));
        return new CustomUserDetails(user);
    }
}
