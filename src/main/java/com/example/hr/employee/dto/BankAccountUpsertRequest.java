package com.example.hr.employee.dto;

public record BankAccountUpsertRequest(
        String bankName,
        String accountName,
        String accountNumber,
        String branch
) {}