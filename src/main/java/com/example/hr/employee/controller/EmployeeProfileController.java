package com.example.hr.employee.controller;

import com.example.hr.employee.dto.*;
import com.example.hr.employee.entity.*;
import com.example.hr.employee.service.EmployeeProfileService;
import com.example.hr.users.entity.User;
import com.example.hr.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeProfileController {

    private final EmployeeProfileService service;
    private final UserRepository userRepository;

    public EmployeeProfileController(EmployeeProfileService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private UUID currentUserId(Authentication auth) {
        // If auth.getName() is email (common JWT), this works:
        String email = auth.getName();
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found for token email: " + email))
                .getId();
    }

    private EmployeeProfileResponse toResponse(User user,
                                              EmployeeProfile profile,
                                              Optional<EmployeeBankAccount> bank,
                                              List<EmergencyContact> contacts) {

        EmployeeProfileResponse.BankAccount bankDto = bank.map(b ->
                new EmployeeProfileResponse.BankAccount(
                        b.getBankName(), b.getAccountName(), b.getAccountNumber(), b.getBranch()
                )).orElse(null);

        List<EmployeeProfileResponse.EmergencyContact> contactDtos = contacts.stream()
                .map(c -> new EmployeeProfileResponse.EmergencyContact(
                        c.getId(), c.getName(), c.getRelationship(), c.getPhone(), c.getAddress()
                ))
                .collect(Collectors.toList());

        return new EmployeeProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),

                profile.getDateOfBirth(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getNrc(),
                profile.getGender(),

                profile.getJobTitle(),
                profile.getDepartment(),
                profile.getJoinDate(),
                profile.getManagerUserId(),

                bankDto,
                contactDtos
        );
    }

    // Admin/Manager: view any employee
    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeProfile(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        EmployeeProfile profile = service.getProfile(userId);
        Optional<EmployeeBankAccount> bank = service.getBank(userId);
        List<EmergencyContact> contacts = service.getEmergencyContacts(userId);

        return ResponseEntity.ok(toResponse(user, profile, bank, contacts));
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EmployeeProfileResponse> upsertEmployeeProfile(
            @PathVariable UUID userId,
            @RequestBody EmployeeProfileUpsertRequest req
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        EmployeeProfile profile = service.upsertProfile(userId, req);
        Optional<EmployeeBankAccount> bank = service.getBank(userId);
        List<EmergencyContact> contacts = service.getEmergencyContacts(userId);

        return ResponseEntity.ok(toResponse(user, profile, bank, contacts));
    }

    // Employee: self endpoints
    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileResponse> me(Authentication auth) {
        UUID userId = currentUserId(auth);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        EmployeeProfile profile = service.getProfile(userId);
        Optional<EmployeeBankAccount> bank = service.getBank(userId);
        List<EmergencyContact> contacts = service.getEmergencyContacts(userId);

        return ResponseEntity.ok(toResponse(user, profile, bank, contacts));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileResponse> upsertMyProfile(
            Authentication auth,
            @RequestBody EmployeeProfileUpsertRequest req
    ) {
        UUID userId = currentUserId(auth);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        EmployeeProfile profile = service.upsertProfile(userId, req);
        Optional<EmployeeBankAccount> bank = service.getBank(userId);
        List<EmergencyContact> contacts = service.getEmergencyContacts(userId);

        return ResponseEntity.ok(toResponse(user, profile, bank, contacts));
    }

    @PutMapping("/me/bank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upsertMyBank(Authentication auth, @RequestBody BankAccountUpsertRequest req) {
        UUID userId = currentUserId(auth);
        service.upsertBank(userId, req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/me/emergency-contacts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> replaceMyEmergencyContacts(
            Authentication auth,
            @RequestBody List<EmergencyContactRequest> reqs
    ) {
        UUID userId = currentUserId(auth);
        service.replaceEmergencyContacts(userId, reqs);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
