package com.example.hr.attendance.repository;

import com.example.hr.attendance.entity.Attendance;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends BaseRepository<Attendance, UUID> {


    boolean existsByEmployeeIdAndCheckInBetween(UUID employeeId, Instant startOfDay, Instant endOfDay);

    boolean existsByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    Optional<Attendance> findTopByEmployeeIdOrderByCheckInDesc(UUID empId);

}
