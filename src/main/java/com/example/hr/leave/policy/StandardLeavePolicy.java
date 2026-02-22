package com.example.hr.leave.policy;

import com.example.hr.leave.api.exception.InsufficientLeaveBalanceException;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Standard annual leave policy.
 * Validates that the employee has sufficient remaining balance.
 */
@Component
public class StandardLeavePolicy implements LeavePolicyStrategy {

    @Override
    public BigDecimal calculateApprovedDays(LeaveRequest request, LeaveBalance balance) {
        BigDecimal requested = request.getRequestedDays();
        BigDecimal remaining = balance.getRemainingDays();

        if (requested.compareTo(remaining) > 0) {
            throw new InsufficientLeaveBalanceException(
                    "Requested %.1f days but only %.1f days remaining".formatted(
                            requested.doubleValue(), remaining.doubleValue()));
        }
        return requested;
    }

    @Override
    public String supportedLeaveType() {
        return "ANNUAL";
    }
}
