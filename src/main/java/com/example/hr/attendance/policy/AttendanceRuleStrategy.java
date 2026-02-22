package com.example.hr.attendance.policy;

import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Strategy interface for evaluating attendance status.
 *
 * <p>
 * Each implementation handles a specific {@code policyType} (e.g. "STANDARD",
 * "REMOTE_WORK").
 * Spring collects all implementations automatically via
 * {@code List<AttendanceRuleStrategy>}
 * injection in {@code AttendanceService}, which then builds a dispatch map
 * keyed by
 * {@link #supportedPolicyType()}.
 *
 * <p>
 * Implementations must be stateless Spring {@code @Component} beans.
 */
public interface AttendanceRuleStrategy {

    /**
     * Evaluates the attendance status for a given check-in time against the
     * configured policy.
     *
     * @param policy      the attendance policy for the employee's department
     * @param checkInTime the actual check-in timestamp (UTC)
     * @param timezone    the timezone to use for local-time comparison
     * @return the computed {@link AttendanceStatus}
     */
    AttendanceStatus evaluate(AttendancePolicy policy, Instant checkInTime, ZoneId timezone);

    /**
     * Returns the {@code policyType} value this strategy can handle.
     * Must match the {@code AttendancePolicy#policyType} column value exactly.
     */
    String supportedPolicyType();
}
