package com.example.hr.auth.dto;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String email,
        Set<String> roles
) {}
