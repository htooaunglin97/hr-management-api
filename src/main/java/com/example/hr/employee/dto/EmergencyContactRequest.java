package com.example.hr.employee.dto;

public record EmergencyContactRequest(
        String name,
        String relationship,
        String phone,
        String address
) {}