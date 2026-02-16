package com.example.hr.employee.dto;


import java.util.UUID;

public record EmployeeSummaryResponse(
        UUID id,
        String name,
        String email,
        boolean active
) {}
