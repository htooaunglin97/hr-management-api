package com.example.hr.attendance.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckInRequest(
        @NotNull  UUID empId
) {
}
