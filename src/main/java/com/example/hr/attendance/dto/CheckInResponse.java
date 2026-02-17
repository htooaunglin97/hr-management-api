package com.example.hr.attendance.dto;

import com.example.hr.attendance.entity.Attendance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CheckInResponse(
        UUID attendanceId,
        UUID employeeId,
        Instant checkInTime
) {
    public static CheckInResponse fromEntity(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        return new CheckInResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getCheckIn()
        );
    }

    public static List<CheckInResponse> fromEntities(List<Attendance> attendances) {
        return attendances.stream()
                .map(CheckInResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
