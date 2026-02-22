package com.example.hr.leave.service;

import com.example.hr.leave.api.exception.LeaveRequestNotFoundException;
import com.example.hr.leave.dto.LeaveApprovalDto;
import com.example.hr.leave.dto.LeaveRequestDto;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.leave.entity.LeaveStatus;
import com.example.hr.leave.policy.LeavePolicyStrategy;
import com.example.hr.leave.repository.LeaveBalanceRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.leave.repository.LeaveTypeRepository;
import com.example.hr.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core service owning all leave management business logic.
 *
 * <p>
 * <b>Design choices:</b>
 * <ul>
 * <li>Balance deduction happens <em>only on APPROVED</em>, not on applying.
 * This prevents
 * race conditions where concurrent approval of two requests for the same
 * employee
 * could both pass the balance check.
 * <li>The {@link LeavePolicyStrategy} dispatch map follows the same Strategy
 * Pattern
 * as {@code AttendanceService} — zero service changes needed to add new leave
 * types.
 * <li>All query operations are read-only annotated to leverage connection pool
 * optimisations.
 * </ul>
 */
@Service
@Transactional
@Slf4j
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final LeaveTypeRepository leaveTypeRepo;

    /**
     * Dispatch map: policyType → strategy. Spring injects all
     * {@link LeavePolicyStrategy} beans.
     */
    private final Map<String, LeavePolicyStrategy> policies;

    public LeaveService(LeaveRequestRepository leaveRequestRepo,
            LeaveBalanceRepository leaveBalanceRepo,
            LeaveTypeRepository leaveTypeRepo,
            List<LeavePolicyStrategy> policyList) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.leaveTypeRepo = leaveTypeRepo;
        this.policies = policyList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        LeavePolicyStrategy::supportedLeaveType, p -> p));
    }

    // ── Apply for Leave ───────────────────────────────────────────────────────

    /**
     * Submits a leave request in PENDING state. Balance is NOT deducted until
     * approval.
     */
    public LeaveRequestDto applyLeave(LeaveRequestDto dto) {
        // 1. Fetch the leave type to resolve policyType
        var leaveType = leaveTypeRepo.findById(dto.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + dto.leaveTypeId()));

        // 2. Fetch current balance
        int currentYear = Year.now().getValue();
        LeaveBalance balance = leaveBalanceRepo
                .findByEmployeeIdAndLeaveTypeIdAndYear(dto.employeeId(), dto.leaveTypeId(), currentYear)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No leave balance for employee %s, type %s, year %d"
                                .formatted(dto.employeeId(), leaveType.getName(), currentYear)));

        // 3. Build request entity (state = PENDING)
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(dto.employeeId());
        request.setLeaveTypeId(dto.leaveTypeId());
        request.setStartDate(dto.startDate());
        request.setEndDate(dto.endDate());
        request.setRequestedDays(dto.requestedDays());
        request.setReason(dto.reason());
        request.setStatus(LeaveStatus.PENDING);

        // 4. Validate via Strategy (throws if insufficient balance / policy violation)
        LeavePolicyStrategy policy = policies.getOrDefault(
                leaveType.getPolicyType(), policies.get("ANNUAL"));
        BigDecimal approvedDays = policy.calculateApprovedDays(request, balance);
        request.setRequestedDays(approvedDays); // strategy may adjust the value

        leaveRequestRepo.save(request);

        log.info("[LEAVE] applied employee={} type={} days={} status=PENDING",
                dto.employeeId(), leaveType.getName(), approvedDays);

        return LeaveRequestDto.fromEntity(request);
    }

    // ── Approve / Reject ──────────────────────────────────────────────────────

    /**
     * Processes a manager's approval or rejection.
     * Balance is deducted <em>only</em> when approved.
     */
    public LeaveRequestDto processApproval(UUID requestId, LeaveApprovalDto approval) {
        LeaveRequest request = leaveRequestRepo.findById(requestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found: " + requestId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be reviewed. Current status: " + request.getStatus());
        }

        request.setReviewedBy(approval.reviewerId());
        request.setReviewedAt(Instant.now());

        if (Boolean.TRUE.equals(approval.approved())) {
            request.setStatus(LeaveStatus.APPROVED);

            // Deduct balance only on approval
            LeaveBalance balance = leaveBalanceRepo
                    .findByEmployeeIdAndLeaveTypeIdAndYear(
                            request.getEmployeeId(), request.getLeaveTypeId(), Year.now().getValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
            balance.setUsedDays(balance.getUsedDays().add(request.getRequestedDays()));
            leaveBalanceRepo.save(balance);

            log.info("[LEAVE] approved requestId={} employee={} days={} reviewer={}",
                    requestId, request.getEmployeeId(), request.getRequestedDays(), approval.reviewerId());
        } else {
            request.setStatus(LeaveStatus.REJECTED);
            log.info("[LEAVE] rejected requestId={} employee={} reviewer={}",
                    requestId, request.getEmployeeId(), approval.reviewerId());
        }

        leaveRequestRepo.save(request);
        return LeaveRequestDto.fromEntity(request);
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    /**
     * Employee cancels their own PENDING leave request. Approved leaves cannot be
     * cancelled here.
     */
    public LeaveRequestDto cancelLeave(UUID requestId, UUID employeeId) {
        LeaveRequest request = leaveRequestRepo.findById(requestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found: " + requestId));

        if (!request.getEmployeeId().equals(employeeId)) {
            throw new SecurityException("Employees can only cancel their own leave requests");
        }
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be cancelled");
        }

        request.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepo.save(request);

        log.info("[LEAVE] cancelled requestId={} employee={}", requestId, employeeId);
        return LeaveRequestDto.fromEntity(request);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<LeaveRequestDto> getEmployeeLeaveHistory(UUID employeeId, Pageable pageable) {
        return leaveRequestRepo.findByEmployeeId(employeeId, pageable)
                .map(LeaveRequestDto::fromEntity);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<LeaveRequestDto> getPendingRequests(Pageable pageable) {
        return leaveRequestRepo.findByStatus(LeaveStatus.PENDING, pageable)
                .map(LeaveRequestDto::fromEntity);
    }
}
