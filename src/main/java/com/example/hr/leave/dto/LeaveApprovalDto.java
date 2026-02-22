package com.example.hr.leave.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for manager approval / rejection of a leave request.
 */
public record LeaveApprovalDto(
        @NotNull UUID reviewerId,
        @NotNull Boolean approved,
        String comment) {
}
