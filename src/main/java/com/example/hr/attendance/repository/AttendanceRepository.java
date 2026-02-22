package com.example.hr.attendance.repository;

import com.example.hr.attendance.entity.Attendance;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends BaseRepository<Attendance, UUID> {

    /**
     * Used as a secondary check (primary guard is Redis SETNX).
     * The DB-level UNIQUE constraint also prevents duplicates — defence in depth.
     */
    boolean existsByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    /**
     * Finds the open (not yet checked-out) record for today.
     */
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND a.date = :date AND a.checkOut IS NULL")
    Optional<Attendance> findTodayOpenRecord(UUID employeeId, LocalDate date);
}
