package com.example.hr.leave.repository;

import com.example.hr.leave.entity.LeaveType;
import com.example.hr.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends BaseRepository<LeaveType, UUID> {

    Optional<LeaveType> findByName(String name);
}
