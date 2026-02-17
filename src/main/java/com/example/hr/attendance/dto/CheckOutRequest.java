package com.example.hr.attendance.dto;

import java.util.UUID;

public record CheckOutRequest(
        UUID empId
) {
}
