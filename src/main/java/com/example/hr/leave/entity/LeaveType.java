package com.example.hr.leave.entity;

import com.example.hr.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Catalogue of leave types (Annual, Sick, Unpaid, Maternity, etc.).
 * Managed by HR Admin; employees reference this when submitting requests.
 */
@Entity
@Table(name = "leave_types")
@Getter
@Setter
public class LeaveType extends MasterEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name; // e.g. "ANNUAL", "SICK", "UNPAID", "MATERNITY"

    /** Whether the leave is paid. */
    @Column(nullable = false)
    private boolean isPaid = true;

    /**
     * Whether supporting documents are required (e.g. medical cert for sick leave).
     */
    @Column(nullable = false)
    private boolean requiresDocs = false;

    /**
     * Default entitlement days per year for this type.
     * Individual employees may have different allocations in {@link LeaveBalance}.
     */
    @Column(nullable = false)
    private int defaultDaysPerYear = 0;

    /**
     * The policy type string used to look up the correct
     * {@link com.example.hr.leave.policy.LeavePolicyStrategy}.
     * e.g. "ANNUAL", "SICK".
     */
    @Column(nullable = false, length = 50)
    private String policyType;
}
