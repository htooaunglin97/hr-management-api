package com.example.hr.attendance.service;

import com.example.hr.attendance.api.exception.NoCheckedInFoundException;
import com.example.hr.attendance.api.exception.UserAlreadyCheckedInException;
import com.example.hr.attendance.dto.CheckInRequest;
import com.example.hr.attendance.dto.CheckInResponse;
import com.example.hr.attendance.dto.CheckOutRequest;
import com.example.hr.attendance.dto.CheckOutResponse;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public CheckInResponse clockIn(CheckInRequest request) {

        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant endOfToday = startOfToday.plus(Duration.ofDays(1)).minusNanos(1);

        if (attendanceRepository.existsByEmployeeIdAndCheckInBetween(
                request.empId(),
                startOfToday,
                endOfToday)
        ) {
            throw new UserAlreadyCheckedInException("Already Clocked In");
        }

        Instant now = Instant.now();

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(request.empId());
        attendance.setCheckIn(now);

        attendanceRepository.save(attendance);

        return CheckInResponse.fromEntity(attendance);
    }

    public CheckOutResponse clockOut(CheckOutRequest request) {

        Attendance attendance = attendanceRepository.findTopByEmployeeIdOrderByCheckInDesc(request.empId())
                .filter(a -> a.getCheckOut() == null)
                .orElseThrow(() -> new NoCheckedInFoundException("No checked-in record found"));

        var checkOutTime = Instant.now();

        attendance.setCheckOut(checkOutTime);
        attendance.setWorkMinutes(Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes());

        attendanceRepository.save(attendance);
        return CheckOutResponse.fromEntity(attendance);
    }

}
