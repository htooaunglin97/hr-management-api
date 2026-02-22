package com.example.hr.leave.policy;

import com.example.hr.leave.api.exception.InsufficientLeaveBalanceException;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class ProbationaryLeavePolicy implements LeavePolicyStrategy {

    private static final BigDecimal MAX_DAYS = BigDecimal.valueOf(3);

    @Override
    public BigDecimal calculateApprovedDays(LeaveRequest request, LeaveBalance balance) {
        BigDecimal requested = request.getRequestedDays();

        if (requested.compareTo(MAX_DAYS) > 0) {
            throw new InsufficientLeaveBalanceException(
                    "Probationary employees may not exceed %.0f days of leave per request".formatted(
                            MAX_DAYS.doubleValue()));
        }

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
        return "PROBATIONARY";
    }
}
