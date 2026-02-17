package com.example.hr.attendance.api.controller;

import com.example.hr.attendance.dto.CheckInRequest;
import com.example.hr.attendance.dto.CheckInResponse;
import com.example.hr.attendance.service.AttendanceService;
import com.example.hr.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/me/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("clock-in")
    public ApiResponse<CheckInResponse> clockIn(CheckInRequest request) {
        CheckInResponse resp = attendanceService.clockIn(request);
        return ApiResponse.success(resp, "Clocked In");
    }
}
