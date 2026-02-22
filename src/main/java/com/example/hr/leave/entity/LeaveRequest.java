package com.example.hr.leave.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single leave application by an employee.
 */
@Entity
@Table(name = "leave_requests")
@SQLDelete(sql = "UPDATE leave_requests SET is_deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class LeaveRequest extends MasterEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Computed business days (excluding weekends/holidays). */
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal requestedDays;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    /** Manager/HR who approved or rejected this request. */
    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    private Instant reviewedAt;
}
