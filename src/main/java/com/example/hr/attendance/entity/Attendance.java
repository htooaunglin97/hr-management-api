package com.example.hr.attendance.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendances", uniqueConstraints = @UniqueConstraint(name = "uq_attendance_employee_date", columnNames = {
        "employee_id", "date" }))
@SQLDelete(sql = "UPDATE attendances SET is_deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Attendance extends MasterEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Instant checkIn;

    private Instant checkOut;

    private Long workMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.PRESENT;
}
