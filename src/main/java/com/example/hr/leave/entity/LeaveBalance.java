package com.example.hr.leave.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tracks each employee's remaining leave balance for a given leave type in a
 * given year.
 * One row per (employee, leaveType, year).
 */
@Entity
@Table(name = "leave_balances", uniqueConstraints = @UniqueConstraint(name = "uq_leave_balance_emp_type_year", columnNames = {
        "employee_id", "leave_type_id", "year" }))
@Getter
@Setter
public class LeaveBalance extends MasterEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(nullable = false)
    private int year;

    /** Total entitlement for the year (can differ per employee). */
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays;

    /** Days consumed by approved leave requests. */
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal usedDays = BigDecimal.ZERO;

    public BigDecimal getRemainingDays() {
        return totalDays.subtract(usedDays);
    }
}
