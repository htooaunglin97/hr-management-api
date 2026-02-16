package com.example.hr.employee.repository;


import com.example.hr.employee.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {}
