package com.example.hr.attendance.repository;

import com.example.hr.attendance.entity.AttendancePolicy;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendancePolicyRepository extends BaseRepository<AttendancePolicy, UUID> {

    /**
     * Find the policy for the given department, falling back to the global default
     * (departmentId IS NULL).
     * JPQL orders department-specific before global so the first result wins.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT p FROM AttendancePolicy p
            WHERE p.departmentId = :departmentId
               OR p.departmentId IS NULL
            ORDER BY p.departmentId NULLS LAST
            LIMIT 1
            """)
    Optional<AttendancePolicy> findByDepartmentIdOrDefault(UUID departmentId);
}
