package com.example.hr.leave.repository;

import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends BaseRepository<LeaveBalance, UUID> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(UUID employeeId, UUID leaveTypeId, int year);
}
