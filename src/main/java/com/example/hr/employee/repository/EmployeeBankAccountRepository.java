package com.example.hr.employee.repository;
import com.example.hr.employee.entity.EmployeeBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeBankAccountRepository extends JpaRepository<EmployeeBankAccount, UUID> {}
