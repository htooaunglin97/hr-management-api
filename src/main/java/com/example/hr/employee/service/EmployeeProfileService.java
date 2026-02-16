package com.example.hr.employee.service;

import com.example.hr.employee.dto.*;
import com.example.hr.employee.entity.*;
import com.example.hr.employee.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EmployeeProfileService {

    private final EmployeeProfileRepository profileRepo;
    private final EmployeeBankAccountRepository bankRepo;
    private final EmergencyContactRepository contactRepo;

    public EmployeeProfileService(EmployeeProfileRepository profileRepo,
                                  EmployeeBankAccountRepository bankRepo,
                                  EmergencyContactRepository contactRepo) {
        this.profileRepo = profileRepo;
        this.bankRepo = bankRepo;
        this.contactRepo = contactRepo;
    }

    public EmployeeProfile getProfile(UUID userId) {
        // create empty profile row if not exists (safe default)
        return profileRepo.findById(userId).orElseGet(() -> {
            EmployeeProfile p = new EmployeeProfile();
            p.setUserId(userId);
            return profileRepo.save(p);
        });
    }

    public Optional<EmployeeBankAccount> getBank(UUID userId) {
        return bankRepo.findById(userId);
    }

    public List<EmergencyContact> getEmergencyContacts(UUID userId) {
        return contactRepo.findByUserIdOrderByIdAsc(userId);
    }

    @Transactional
    public EmployeeProfile upsertProfile(UUID userId, EmployeeProfileUpsertRequest req) {
        EmployeeProfile profile = getProfile(userId);

        profile.setDateOfBirth(req.dateOfBirth());
        profile.setPhone(req.phone());
        profile.setAddress(req.address());
        profile.setNrc(req.nrc());
        profile.setGender(req.gender());

        profile.setJobTitle(req.jobTitle());
        profile.setDepartment(req.department());
        profile.setJoinDate(req.joinDate());
        profile.setManagerUserId(req.managerUserId());

        return profileRepo.save(profile);
    }

    @Transactional
    public void upsertBank(UUID userId, BankAccountUpsertRequest req) {
        EmployeeBankAccount bank = bankRepo.findById(userId).orElseGet(() -> {
            EmployeeBankAccount b = new EmployeeBankAccount();
            b.setUserId(userId);
            return b;
        });

        bank.setBankName(req.bankName());
        bank.setAccountName(req.accountName());
        bank.setAccountNumber(req.accountNumber());
        bank.setBranch(req.branch());

        bankRepo.save(bank);
    }

    @Transactional
    public void replaceEmergencyContacts(UUID userId, List<EmergencyContactRequest> reqs) {
        contactRepo.deleteByUserId(userId);

        if (reqs == null) return;

        for (EmergencyContactRequest r : reqs) {
            if (r == null || r.name() == null || r.name().isBlank()) continue;

            EmergencyContact c = new EmergencyContact();
            c.setUserId(userId);
            c.setName(r.name().trim());
            c.setRelationship(r.relationship());
            c.setPhone(r.phone());
            c.setAddress(r.address());
            contactRepo.save(c);
        }
    }
}
