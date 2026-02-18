package com.example.hr.attendance.dto;

import com.example.hr.attendance.entity.Attendance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CheckOutResponse(
        UUID attendanceId,
        UUID employeeId,
        Instant checkOutTime,
        Long workMinutes
) {
    public static CheckOutResponse fromEntity(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        return new CheckOutResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getCheckOut(),
                attendance.getWorkMinutes()
        );
    }

    public static List<CheckOutResponse> fromEntities(List<Attendance> attendances) {
        return attendances.stream()
                .map(CheckOutResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
