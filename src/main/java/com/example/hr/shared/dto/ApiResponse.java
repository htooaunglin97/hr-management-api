package com.example.hr.shared.dto;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    // Static helper method ကို record ထဲမှာလည်း ရေးလို့ရပါတယ်
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}
