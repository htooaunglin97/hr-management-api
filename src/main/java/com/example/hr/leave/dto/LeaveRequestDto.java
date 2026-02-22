package com.example.hr.leave.dto;

import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.leave.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO transferring leave request data between Controller and Service.
 * Used for both incoming requests and outgoing responses.
 */
public record LeaveRequestDto(
        UUID id,
        @NotNull UUID employeeId,
        @NotNull UUID leaveTypeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Positive BigDecimal requestedDays,
        String reason,
        LeaveStatus status,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt) {
    public static LeaveRequestDto fromEntity(LeaveRequest r) {
        return new LeaveRequestDto(
                r.getId(),
                r.getEmployeeId(),
                r.getLeaveTypeId(),
                r.getStartDate(),
                r.getEndDate(),
                r.getRequestedDays(),
                r.getReason(),
                r.getStatus(),
                r.getReviewedBy(),
                r.getReviewedAt(),
                r.getCreatedAt());
    }
}
