package com.example.hr.attendance.dto;

import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CheckOutResponse(
        UUID attendanceId,
        UUID employeeId,
        LocalDate date,
        Instant checkInTime,
        Instant checkOutTime,
        Long workMinutes,
        AttendanceStatus status) {
    public static CheckOutResponse fromEntity(Attendance attendance) {
        if (attendance == null)
            return null;
        return new CheckOutResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getDate(),
                attendance.getCheckIn(),
                attendance.getCheckOut(),
                attendance.getWorkMinutes(),
                attendance.getStatus());
    }
}
