package com.example.hr.attendance.policy;

import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.attendance.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Remote-work attendance rule.
 *
 * <p>
 * Because remote employees are not tied to a physical office, they are always
 * considered PRESENT as long as they check in before {@code shiftEnd}.
 * Checking in after {@code shiftEnd} is still marked LATE.
 *
 * <p>
 * This implementation demonstrates how adding a new work-mode policy requires
 * zero changes to {@code AttendanceService} — simply register a new
 * {@code @Component}.
 */
@Component
public class RemoteWorkAttendanceRule implements AttendanceRuleStrategy {

    @Override
    public AttendanceStatus evaluate(AttendancePolicy policy, Instant checkInTime, ZoneId timezone) {
        var actual = checkInTime.atZone(timezone).toLocalTime();
        var cutoff = policy.getShiftEnd();
        return actual.isAfter(cutoff) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
    }

    @Override
    public String supportedPolicyType() {
        return "REMOTE_WORK";
    }
}
