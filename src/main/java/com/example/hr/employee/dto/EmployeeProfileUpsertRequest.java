package com.example.hr.employee.dto;


import java.time.LocalDate;
import java.util.UUID;

public record EmployeeProfileUpsertRequest(
        LocalDate dateOfBirth,
        String phone,
        String address,
        String nrc,
        String gender,

        String jobTitle,
        String department,
        LocalDate joinDate,
        UUID managerUserId
) {}

