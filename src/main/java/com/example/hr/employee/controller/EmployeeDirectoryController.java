package com.example.hr.employee.controller;

import com.example.hr.employee.dto.EmployeeSummaryResponse;
import com.example.hr.users.entity.User;
import com.example.hr.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeDirectoryController {

    private final UserRepository userRepository;

    public EmployeeDirectoryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EmployeeSummaryResponse>> list(
            @RequestParam(value = "query", required = false) String query
    ) {
        List<User> users = (query == null || query.isBlank())
                ? userRepository.findByIsDeletedFalse()
                : userRepository.searchActiveByNameOrEmail(query.trim());

        List<EmployeeSummaryResponse> result = users.stream()
                .map(u -> new EmployeeSummaryResponse(u.getId(), u.getName(), u.getEmail(), u.isActive()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
