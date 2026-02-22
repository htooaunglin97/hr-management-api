package com.example.hr.attendance.service;

import com.example.hr.attendance.api.exception.NoCheckedInFoundException;
import com.example.hr.attendance.api.exception.UserAlreadyCheckedInException;
import com.example.hr.attendance.dto.CheckInResponse;
import com.example.hr.attendance.dto.CheckOutResponse;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.attendance.entity.AttendanceStatus;
import com.example.hr.attendance.policy.StandardAttendanceRule;
import com.example.hr.attendance.repository.AttendancePolicyRepository;
import com.example.hr.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private AttendancePolicyRepository policyRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    private AttendanceService sut;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        sut = new AttendanceService(
                attendanceRepository,
                policyRepository,
                redisTemplate,
                List.of(new StandardAttendanceRule())
        );
    }

    // ── clockIn ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clockIn: happy path → saves attendance with employeeId")
    void clockIn_success_savesAttendance() {
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        when(valueOps.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);
        when(policyRepository.findByDepartmentIdOrDefault(departmentId))
                .thenReturn(Optional.of(defaultPolicy()));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CheckInResponse response = sut.clockIn(employeeId, departmentId);

        assertThat(response.employeeId()).isEqualTo(employeeId);
        assertThat(response.date()).isEqualTo(LocalDate.now(java.time.ZoneId.of("Asia/Yangon")));
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("clockIn: Redis lock NOT acquired → throws UserAlreadyCheckedInException, no DB call")
    void clockIn_duplicateRequest_throwsException() {
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        when(valueOps.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> sut.clockIn(employeeId, departmentId))
                .isInstanceOf(UserAlreadyCheckedInException.class)
                .hasMessageContaining("Already clocked in today");

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    @DisplayName("clockIn: shiftStart far in future → status is PRESENT")
    void clockIn_earlyCheckIn_statusPresent() {
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        AttendancePolicy policy = defaultPolicy();
        policy.setShiftStart(LocalTime.of(23, 59)); // always PRESENT
        when(valueOps.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);
        when(policyRepository.findByDepartmentIdOrDefault(departmentId)).thenReturn(Optional.of(policy));
        when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CheckInResponse response = sut.clockIn(employeeId, departmentId);

        assertThat(response.status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    // ── clockOut ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clockOut: open record exists → sets checkOut and workMinutes")
    void clockOut_success_setsCheckOut() {
        UUID employeeId = UUID.randomUUID();

        Attendance open = openAttendance(employeeId, 3600); // checked in 1h ago

        when(attendanceRepository.findTodayOpenRecord(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(open));
        when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CheckOutResponse response = sut.clockOut(employeeId);

        assertThat(response.workMinutes()).isGreaterThan(0);
        assertThat(response.checkOutTime()).isNotNull();
    }

    @Test
    @DisplayName("clockOut: worked < 4 hours → status downgraded to HALF_DAY")
    void clockOut_shortDay_statusHalfDay() {
        UUID employeeId = UUID.randomUUID();

        Attendance open = openAttendance(employeeId, 60); // 1 minute ago

        when(attendanceRepository.findTodayOpenRecord(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(open));
        when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CheckOutResponse response = sut.clockOut(employeeId);

        assertThat(response.status()).isEqualTo(AttendanceStatus.HALF_DAY);
    }

    @Test
    @DisplayName("clockOut: no open record → throws NoCheckedInFoundException")
    void clockOut_noOpenRecord_throwsException() {
        UUID employeeId = UUID.randomUUID();

        when(attendanceRepository.findTodayOpenRecord(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.clockOut(employeeId))
                .isInstanceOf(NoCheckedInFoundException.class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AttendancePolicy defaultPolicy() {
        AttendancePolicy p = new AttendancePolicy();
        p.setPolicyType("STANDARD");
        p.setShiftStart(LocalTime.of(9, 0));
        p.setShiftEnd(LocalTime.of(17, 0));
        p.setLateGraceMinutes(15);
        return p;
    }

    private Attendance openAttendance(UUID employeeId, long secondsAgo) {
        Attendance a = new Attendance();
        a.setEmployeeId(employeeId);
        a.setCheckIn(Instant.now().minusSeconds(secondsAgo));
        a.setStatus(AttendanceStatus.PRESENT);
        return a;
    }
}
