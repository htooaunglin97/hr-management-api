package com.example.hr.attendance.policy;

import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.attendance.entity.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests — no Spring context, no DB.
 * Fast feedback on the rule evaluation logic.
 */
class StandardAttendanceRuleTest {

    private static final ZoneId TZ = ZoneId.of("Asia/Yangon");

    private StandardAttendanceRule rule;

    @BeforeEach
    void setUp() {
        rule = new StandardAttendanceRule();
    }

    @Test
    @DisplayName("supportedPolicyType returns STANDARD")
    void supportsPolicyType() {
        assertThat(rule.supportedPolicyType()).isEqualTo("STANDARD");
    }

    @Test
    @DisplayName("Check-in exactly at shiftStart → PRESENT")
    void checkIn_atShiftStart_returnsPresent() {
        AttendancePolicy policy = policyWith(LocalTime.of(9, 0), 15);
        Instant checkIn = todayAt(LocalTime.of(9, 0), TZ);

        assertThat(rule.evaluate(policy, checkIn, TZ)).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Check-in within grace period → PRESENT")
    void checkIn_withinGrace_returnsPresent() {
        AttendancePolicy policy = policyWith(LocalTime.of(9, 0), 15);
        Instant checkIn = todayAt(LocalTime.of(9, 14), TZ);

        assertThat(rule.evaluate(policy, checkIn, TZ)).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Check-in exactly at grace boundary → PRESENT")
    void checkIn_atGraceBoundary_returnsPresent() {
        AttendancePolicy policy = policyWith(LocalTime.of(9, 0), 15);
        Instant checkIn = todayAt(LocalTime.of(9, 15), TZ);

        assertThat(rule.evaluate(policy, checkIn, TZ)).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Check-in one minute past grace → LATE")
    void checkIn_pastGrace_returnsLate() {
        AttendancePolicy policy = policyWith(LocalTime.of(9, 0), 15);
        Instant checkIn = todayAt(LocalTime.of(9, 16), TZ);

        assertThat(rule.evaluate(policy, checkIn, TZ)).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("Very late check-in → LATE")
    void checkIn_veryLate_returnsLate() {
        AttendancePolicy policy = policyWith(LocalTime.of(9, 0), 15);
        Instant checkIn = todayAt(LocalTime.of(12, 0), TZ);

        assertThat(rule.evaluate(policy, checkIn, TZ)).isEqualTo(AttendanceStatus.LATE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AttendancePolicy policyWith(LocalTime shiftStart, int graceMinutes) {
        AttendancePolicy policy = new AttendancePolicy();
        policy.setShiftStart(shiftStart);
        policy.setLateGraceMinutes(graceMinutes);
        return policy;
    }

    private Instant todayAt(LocalTime time, ZoneId zone) {
        return LocalDate.now(zone).atTime(time).atZone(zone).toInstant();
    }
}
