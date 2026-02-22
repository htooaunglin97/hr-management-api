package com.example.hr.attendance.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_policies")
@Getter
@Setter
public class AttendancePolicy extends MasterEntity {

    /**
     * Null means this is the global default policy.
     * Otherwise, links to a department.
     */
    @Column(name = "department_id")
    private UUID departmentId;

    /**
     * Identifies which AttendanceRuleStrategy handles this policy.
     * e.g. "STANDARD", "REMOTE_WORK", "SHIFT"
     */
    @Column(nullable = false, length = 50)
    private String policyType = "STANDARD";

    @Column(nullable = false)
    private LocalTime shiftStart;

    @Column(nullable = false)
    private LocalTime shiftEnd;

    /**
     * Number of minutes of grace allowed after shiftStart before marking LATE.
     */
    @Column(nullable = false)
    private int lateGraceMinutes = 15;

    @Column(nullable = false)
    private boolean allowRemote = false;
}
