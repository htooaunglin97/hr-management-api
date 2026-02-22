package com.example.hr.attendance.policy;

import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.attendance.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Standard office attendance rule.
 *
 * <p>
 * An employee is PRESENT if they check in within the grace period after
 * {@code shiftStart}.
 * Beyond the grace period, the status is LATE.
 */
@Component
public class StandardAttendanceRule implements AttendanceRuleStrategy {

    @Override
    public AttendanceStatus evaluate(AttendancePolicy policy, Instant checkInTime, ZoneId timezone) {
        LocalTime actual = checkInTime.atZone(timezone).toLocalTime();
        LocalTime deadline = policy.getShiftStart().plusMinutes(policy.getLateGraceMinutes());
        return actual.isAfter(deadline) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
    }

    @Override
    public String supportedPolicyType() {
        return "STANDARD";
    }
}
