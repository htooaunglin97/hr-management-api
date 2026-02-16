package com.example.hr.users.service;

import com.example.hr.users.entity.User;
import com.example.hr.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // =========================
    // Employee Directory
    // =========================

    public List<User> getAllActiveEmployees() {
        return userRepository.findByIsDeletedFalse();
    }

    public List<User> searchEmployees(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveEmployees();
        }
        return userRepository.searchActiveByNameOrEmail(query.trim());
    }

    public User getEmployeeById(UUID id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // =========================
    // Profile Management
    // =========================

    public User updateProfile(UUID userId, User updated) {
        User existing = getEmployeeById(userId);

        existing.setName(updated.getName());
        existing.setProfileImageUrl(updated.getProfileImageUrl());
        // later: bank info, emergency contact, etc

        return userRepository.save(existing);
    }
}
