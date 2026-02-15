package com.example.hr.users.repository;

import com.example.hr.users.entity.Role;
import com.example.hr.users.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}
