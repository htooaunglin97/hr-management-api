package com.example.hr.auth.service;

import com.example.hr.auth.entity.PasswordResetToken;
import com.example.hr.auth.repository.PasswordResetTokenRepository;
import com.example.hr.shared.exception.ResourceNotFoundException;
import com.example.hr.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestReset(String email) {
        // Always respond success to avoid leaking whether the email exists
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            PasswordResetToken t = new PasswordResetToken();
            t.setToken(UUID.randomUUID().toString().replace("-", ""));
            t.setUserId(user.getId());
            t.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            tokenRepository.save(t);

            // Dev mode: log token
            System.out.println("PASSWORD RESET TOKEN for " + email + " => " + t.getToken());
            System.out.println("Use it with POST /api/auth/reset-password");
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        if (t.isUsed()) throw new IllegalArgumentException("Token already used");
        if (t.isExpired()) throw new IllegalArgumentException("Token expired");

        var user = userRepository.findById(t.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword_hash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        t.setUsedAt(Instant.now());
        tokenRepository.save(t);
    }
}
