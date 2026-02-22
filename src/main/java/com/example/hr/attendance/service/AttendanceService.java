package com.example.hr.attendance.service;

import com.example.hr.attendance.api.exception.NoCheckedInFoundException;
import com.example.hr.attendance.api.exception.UserAlreadyCheckedInException;
import com.example.hr.attendance.dto.CheckInResponse;
import com.example.hr.attendance.dto.CheckOutResponse;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendanceStatus;
import com.example.hr.attendance.policy.AttendanceRuleStrategy;
import com.example.hr.attendance.repository.AttendancePolicyRepository;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core service owning all attendance business logic.
 *
 * <p>
 * <b>Separation of concerns:</b>
 * <ul>
 * <li>HTTP details (headers, request/response) stay in the Controller layer.
 * <li>DB queries stay in the Repository layer.
 * <li>Rule evaluation is delegated to the injected
 * {@link AttendanceRuleStrategy} — Strategy Pattern.
 * <li>Concurrency safety is handled by Redis SETNX + DB UNIQUE constraint
 * (defence in depth).
 * </ul>
 */
@Service
@Transactional
@Slf4j
public class AttendanceService {

    /**
     * Timezone used for local-time comparisons (Myanmar Standard Time, UTC+6:30).
     * Should be moved to application configuration for flexibility.
     */
    private static final ZoneId APP_TIMEZONE = ZoneId.of("Asia/Yangon");

    private final AttendanceRepository attendanceRepository;
    private final AttendancePolicyRepository policyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Dispatch map: policyType → strategy. Spring injects all
     * {@link AttendanceRuleStrategy} beans.
     */
    private final Map<String, AttendanceRuleStrategy> strategies;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendancePolicyRepository policyRepository,
            RedisTemplate<String, String> redisTemplate,
            List<AttendanceRuleStrategy> strategyList) {
        this.attendanceRepository = attendanceRepository;
        this.policyRepository = policyRepository;
        this.redisTemplate = redisTemplate;
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AttendanceRuleStrategy::supportedPolicyType, s -> s));
    }

    // ── Clock In ─────────────────────────────────────────────────────────────

    /**
     * Records a check-in for the given employee.
     *
     * <p>
     * Guards against duplicate check-ins using a Redis distributed lock (SETNX) so
     * that
     * even under high concurrency at rush hours only one record is persisted per
     * employee per day.
     *
     * @param employeeId   the authenticated employee's UUID (resolved from JWT in
     *                     the Controller)
     * @param departmentId the employee's department UUID (used to look up the
     *                     correct policy)
     * @return the persisted {@link CheckInResponse}
     */
    public CheckInResponse clockIn(UUID employeeId, UUID departmentId) {
        // 1. Redis distributed lock — prevents race condition during check-in rush
        String lockKey = buildLockKey(employeeId);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", Duration.ofMinutes(2)); // TTL > any reasonable request
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[ATTEND] Duplicate clock-in blocked employee={} date={}", employeeId,
                    LocalDate.now(APP_TIMEZONE));
            throw new UserAlreadyCheckedInException("Already clocked in today");
        }

        // 2. Fetch policy for the department (falls back to global default)
        var policy = policyRepository.findByDepartmentIdOrDefault(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No attendance policy found for department: " + departmentId));

        // 3. Resolve the correct rule via Strategy dispatch
        AttendanceRuleStrategy rule = strategies.getOrDefault(
                policy.getPolicyType(),
                strategies.get("STANDARD")); // safe fallback

        Instant now = Instant.now();
        AttendanceStatus status = rule.evaluate(policy, now, APP_TIMEZONE);

        // 4. Persist
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setDate(LocalDate.now(APP_TIMEZONE));
        attendance.setCheckIn(now);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        log.info("[ATTEND] clockIn employee={} date={} status={} policy={}",
                employeeId, attendance.getDate(), status, policy.getPolicyType());

        return CheckInResponse.fromEntity(attendance);
    }

    // ── Clock Out ────────────────────────────────────────────────────────────

    /**
     * Records a check-out for the given employee.
     * Also adjusts status to HALF_DAY if total worked time is less than 4 hours.
     *
     * @param employeeId the authenticated employee's UUID
     * @return the updated {@link CheckOutResponse}
     */
    public CheckOutResponse clockOut(UUID employeeId) {
        LocalDate today = LocalDate.now(APP_TIMEZONE);

        Attendance attendance = attendanceRepository.findTodayOpenRecord(employeeId, today)
                .orElseThrow(() -> new NoCheckedInFoundException("No open check-in record found for today"));

        Instant checkOutTime = Instant.now();
        long workedMinutes = Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes();

        attendance.setCheckOut(checkOutTime);
        attendance.setWorkMinutes(workedMinutes);
        if (workedMinutes < 240) { // less than 4 hours → half day
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }
        attendanceRepository.save(attendance);

        log.info("[ATTEND] clockOut employee={} date={} workedMinutes={} status={}",
                employeeId, today, workedMinutes, attendance.getStatus());

        return CheckOutResponse.fromEntity(attendance);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildLockKey(UUID employeeId) {
        return "hr:attendance:lock:%s:%s".formatted(employeeId, LocalDate.now(APP_TIMEZONE));
    }
}
