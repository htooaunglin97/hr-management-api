package com.example.hr.attendance.api.controller;

import com.example.hr.attendance.dto.CheckInRequest;
import com.example.hr.attendance.dto.CheckInResponse;
import com.example.hr.attendance.dto.CheckOutResponse;
import com.example.hr.attendance.service.AttendanceService;
import com.example.hr.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for attendance operations.
 *
 * <p>
 * This controller is deliberately thin — it only:
 * <ol>
 * <li>Parses and validates the HTTP request.
 * <li>Delegates to {@link AttendanceService}.
 * <li>Wraps the result in the standard {@link ApiResponse}.
 * </ol>
 * No business logic lives here.
 */
@RestController
@RequestMapping("api/me/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * POST /api/me/attendances/clock-in
     * Records today's check-in for the employee.
     */
    @PostMapping("clock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CheckInResponse> clockIn(@Valid @RequestBody CheckInRequest request) {
        CheckInResponse resp = attendanceService.clockIn(request.employeeId(), request.departmentId());
        return ApiResponse.success(resp, "Clocked in successfully");
    }

    /**
     * POST /api/me/attendances/clock-out
     * Records today's check-out for the employee.
     */
    @PostMapping("clock-out")
    public ApiResponse<CheckOutResponse> clockOut(@RequestParam UUID employeeId) {
        CheckOutResponse resp = attendanceService.clockOut(employeeId);
        return ApiResponse.success(resp, "Clocked out successfully");
    }
}
