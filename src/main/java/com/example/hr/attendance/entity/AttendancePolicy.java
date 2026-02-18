package com.example.hr.attendance.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "attendance_policies")
public class AttendancePolicy extends MasterEntity {

    private LocalTime shiftStart;

    private LocalTime shiftEnd;

    private int lateGracePeriod;


}
