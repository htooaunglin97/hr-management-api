package com.example.hr.attendance.service;

import com.example.hr.attendance.api.exception.UserAlreadyCheckedInException;
import com.example.hr.attendance.dto.CheckInRequest;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void shouldThrowUserAlreadyCheckedInExceptionWhenSameUserTriesToCheckInAgain() {

        UUID empId = UUID.randomUUID();
        CheckInRequest request = new CheckInRequest(empId);

        when(attendanceRepository.existsByEmployeeIdAndCheckInBetween(empId, any(), any()))
                .thenReturn(true);

        assertThrows(UserAlreadyCheckedInException.class, () -> {
            attendanceService.clockIn(request);
        });
    }

    @Test
    void shouldCheckInSuccessfully() {
        UUID empId = UUID.randomUUID();
        CheckInRequest request = new CheckInRequest(empId);

        when(attendanceRepository.existsByEmployeeIdAndCheckInBetween(empId, any(), any()))
                .thenReturn(false);

        attendanceService.clockIn(request);

        ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);

        // Act
        attendanceService.clockIn(request);

        // Assert
        verify(attendanceRepository).save(attendanceCaptor.capture());
        Attendance savedAttendance = attendanceCaptor.getValue();


        assertThat(savedAttendance.getEmployeeId()).isEqualTo(empId);
        assertThat(savedAttendance.getCheckIn()).isNotNull();
        assertThat(savedAttendance.getCheckOut()).isNull();
    }



}
