package org.example.mobilyecommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.model.User;
import org.example.mobilyecommerce.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceConfigImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 Loading user by email: {}", email);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            log.warn("❌ User not found with email: {}", email);
            // ✅ رسالة واضحة بدون تفاصيل حساسة
            throw new UsernameNotFoundException("User not found");
        }

        List<GrantedAuthority> grantedAuthorities = user.get().getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
                .collect(Collectors.toList());

        log.debug("✅ User loaded successfully: {}", user.get().getUsername());

        return new org.springframework.security.core.userdetails.User(
                user.get().getEmail(),
                user.get().getPassword(),
                grantedAuthorities
        );
    }
}