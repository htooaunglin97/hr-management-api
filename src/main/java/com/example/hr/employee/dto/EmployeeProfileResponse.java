package com.example.hr.employee.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EmployeeProfileResponse(
        UUID userId,
        String name,
        String email,

        LocalDate dateOfBirth,
        String phone,
        String address,
        String nrc,
        String gender,

        String jobTitle,
        String department,
        LocalDate joinDate,
        UUID managerUserId,

        BankAccount bankAccount,
        List<EmergencyContact> emergencyContacts
) {
    public record BankAccount(
            String bankName,
            String accountName,
            String accountNumber,
            String branch
    ) {}

    public record EmergencyContact(
            Long id,
            String name,
            String relationship,
            String phone,
            String address
    ) {}
}
