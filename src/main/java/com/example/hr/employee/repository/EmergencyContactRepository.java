package com.example.hr.employee.repository;


import com.example.hr.employee.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserIdOrderByIdAsc(UUID userId);
    void deleteByUserId(UUID userId);
}