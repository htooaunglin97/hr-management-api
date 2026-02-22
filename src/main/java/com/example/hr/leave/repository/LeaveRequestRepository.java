package com.example.hr.leave.repository;

import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.leave.entity.LeaveStatus;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends BaseRepository<LeaveRequest, UUID> {

    Page<LeaveRequest> findByEmployeeId(UUID employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    boolean existsByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);
}
