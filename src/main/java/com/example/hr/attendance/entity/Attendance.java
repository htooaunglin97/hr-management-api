package com.example.hr.attendance.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendances")
@Getter
@Setter
public class Attendance extends MasterEntity {

    private UUID employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Instant checkIn;

    private Instant checkOut;

    private Long workMinutes;
}
