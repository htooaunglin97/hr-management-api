package com.example.hr.shared.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        Instant timestamp,
        Map<String, String> details
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(false, status, error, message, Instant.now(), null);
    }
}