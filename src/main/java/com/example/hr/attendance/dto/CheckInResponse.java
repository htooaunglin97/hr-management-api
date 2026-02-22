package com.example.hr.attendance.dto;

import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CheckInResponse(
        UUID attendanceId,
        UUID employeeId,
        LocalDate date,
        Instant checkInTime,
        AttendanceStatus status) {
    public static CheckInResponse fromEntity(Attendance attendance) {
        if (attendance == null)
            return null;
        return new CheckInResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getDate(),
                attendance.getCheckIn(),
                attendance.getStatus());
    }
}
