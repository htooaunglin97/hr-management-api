package com.example.hr.attendance.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for the clock-in endpoint.
 *
 * <p>
 * In production, {@code employeeId} and {@code departmentId} should be resolved
 * from the authenticated JWT principal rather than the request body. They are
 * kept here
 * as explicit fields to simplify the scaffolding example.
 */
public record CheckInRequest(
                @NotNull UUID employeeId,
                @NotNull UUID departmentId) {
}
