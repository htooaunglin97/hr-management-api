package com.example.hr.leave.policy;

import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;

import java.math.BigDecimal;

/**
 * Strategy interface for leave policy rule evaluation.
 *
 * <p>
 * Each implementation validates and calculates approved days for a specific
 * {@code leaveType}, matching the {@code policyType} field on
 * {@link com.example.hr.leave.entity.LeaveType}.
 *
 * <p>
 * Implementations must be stateless Spring {@code @Component} beans.
 */
public interface LeavePolicyStrategy {

    /**
     * Validates the leave request against the employee's balance and policy rules.
     * Returns the number of approved days.
     *
     * @param request the leave request entity (not yet persisted)
     * @param balance the employee's current balance for this leave type
     * @return approved days to be recorded
     * @throws com.example.hr.leave.api.exception.InsufficientLeaveBalanceException if
     *                                                                              balance
     *                                                                              is
     *                                                                              insufficient
     */
    BigDecimal calculateApprovedDays(LeaveRequest request, LeaveBalance balance);

    /**
     * The {@code policyType} value this strategy handles.
     * Must match {@link com.example.hr.leave.entity.LeaveType#getPolicyType()}.
     */
    String supportedLeaveType();
}
