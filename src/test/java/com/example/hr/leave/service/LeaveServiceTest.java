package com.example.hr.leave.service;

import com.example.hr.leave.api.exception.InsufficientLeaveBalanceException;
import com.example.hr.leave.api.exception.LeaveRequestNotFoundException;
import com.example.hr.leave.dto.LeaveApprovalDto;
import com.example.hr.leave.dto.LeaveRequestDto;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.leave.entity.LeaveStatus;
import com.example.hr.leave.entity.LeaveType;
import com.example.hr.leave.policy.StandardLeavePolicy;
import com.example.hr.leave.repository.LeaveBalanceRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.leave.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepo;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepo;
    @Mock
    private LeaveTypeRepository leaveTypeRepo;

    private LeaveService sut;

    @BeforeEach
    void setUp() {
        sut = new LeaveService(leaveRequestRepo, leaveBalanceRepo, leaveTypeRepo,
                List.of(new StandardLeavePolicy()));
    }

    // ── applyLeave ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("applyLeave: sufficient balance → saves request with PENDING status")
    void applyLeave_sufficientBalance_savesPending() {
        UUID empId = UUID.randomUUID();
        UUID leaveTypeId = UUID.randomUUID();

        when(leaveTypeRepo.findById(leaveTypeId)).thenReturn(Optional.of(annualLeaveType(leaveTypeId)));
        when(leaveBalanceRepo.findByEmployeeIdAndLeaveTypeIdAndYear(empId, leaveTypeId, Year.now().getValue()))
                .thenReturn(Optional.of(balanceWith(BigDecimal.TEN)));
        when(leaveRequestRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto result = sut.applyLeave(requestDto(empId, leaveTypeId, BigDecimal.valueOf(3)));

        assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(result.requestedDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
        verify(leaveRequestRepo).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("applyLeave: insufficient balance → throws InsufficientLeaveBalanceException")
    void applyLeave_insufficientBalance_throws() {
        UUID empId = UUID.randomUUID();
        UUID leaveTypeId = UUID.randomUUID();

        when(leaveTypeRepo.findById(leaveTypeId)).thenReturn(Optional.of(annualLeaveType(leaveTypeId)));
        when(leaveBalanceRepo.findByEmployeeIdAndLeaveTypeIdAndYear(empId, leaveTypeId, Year.now().getValue()))
                .thenReturn(Optional.of(balanceWith(BigDecimal.ONE))); // only 1 day left

        assertThatThrownBy(() -> sut.applyLeave(requestDto(empId, leaveTypeId, BigDecimal.TEN)))
                .isInstanceOf(InsufficientLeaveBalanceException.class);

        verifyNoInteractions(leaveRequestRepo);
    }

    // ── processApproval ───────────────────────────────────────────────────────

    @Test
    @DisplayName("processApproval: approved=true → status APPROVED and balance deducted")
    void processApproval_approved_deductsBalance() {
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();
        UUID leaveTypeId = UUID.randomUUID();

        LeaveRequest pending = pendingRequest(empId, leaveTypeId, BigDecimal.valueOf(5));
        LeaveBalance balance = balanceWith(BigDecimal.TEN);

        when(leaveRequestRepo.findById(requestId)).thenReturn(Optional.of(pending));
        when(leaveBalanceRepo.findByEmployeeIdAndLeaveTypeIdAndYear(empId, leaveTypeId, Year.now().getValue()))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto result = sut.processApproval(requestId,
                new LeaveApprovalDto(reviewerId, true, null));

        assertThat(result.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
        verify(leaveBalanceRepo).save(balance);
    }

    @Test
    @DisplayName("processApproval: approved=false → status REJECTED, balance unchanged")
    void processApproval_rejected_noBalanceChange() {
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        LeaveRequest pending = pendingRequest(empId, UUID.randomUUID(), BigDecimal.valueOf(3));
        when(leaveRequestRepo.findById(requestId)).thenReturn(Optional.of(pending));
        when(leaveRequestRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto result = sut.processApproval(requestId,
                new LeaveApprovalDto(reviewerId, false, "Busy period"));

        assertThat(result.status()).isEqualTo(LeaveStatus.REJECTED);
        verifyNoInteractions(leaveBalanceRepo);
    }

    @Test
    @DisplayName("processApproval: request not found → throws LeaveRequestNotFoundException")
    void processApproval_notFound_throws() {
        when(leaveRequestRepo.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.processApproval(UUID.randomUUID(),
                new LeaveApprovalDto(UUID.randomUUID(), true, null)))
                .isInstanceOf(LeaveRequestNotFoundException.class);
    }

    // ── cancelLeave ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelLeave: own PENDING request → status CANCELLED")
    void cancelLeave_ownRequest_cancelled() {
        UUID empId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        LeaveRequest pending = pendingRequest(empId, UUID.randomUUID(), BigDecimal.TWO);
        when(leaveRequestRepo.findById(requestId)).thenReturn(Optional.of(pending));
        when(leaveRequestRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto result = sut.cancelLeave(requestId, empId);

        assertThat(result.status()).isEqualTo(LeaveStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelLeave: another employee's request → throws SecurityException")
    void cancelLeave_otherEmployee_throws() {
        UUID requestId = UUID.randomUUID();
        UUID ownerEmpId = UUID.randomUUID();
        UUID attackerEmpId = UUID.randomUUID();

        LeaveRequest pending = pendingRequest(ownerEmpId, UUID.randomUUID(), BigDecimal.ONE);
        when(leaveRequestRepo.findById(requestId)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> sut.cancelLeave(requestId, attackerEmpId))
                .isInstanceOf(SecurityException.class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LeaveType annualLeaveType(UUID id) {
        LeaveType t = new LeaveType();
        t.setName("ANNUAL");
        t.setPolicyType("ANNUAL");
        t.setPaid(true);
        return t;
    }

    private LeaveBalance balanceWith(BigDecimal total) {
        LeaveBalance b = new LeaveBalance();
        b.setTotalDays(total);
        b.setUsedDays(BigDecimal.ZERO);
        return b;
    }

    private LeaveRequest pendingRequest(UUID empId, UUID leaveTypeId, BigDecimal days) {
        LeaveRequest r = new LeaveRequest();
        r.setEmployeeId(empId);
        r.setLeaveTypeId(leaveTypeId);
        r.setRequestedDays(days);
        r.setStatus(LeaveStatus.PENDING);
        r.setStartDate(LocalDate.now());
        r.setEndDate(LocalDate.now().plusDays(days.intValue() - 1));
        return r;
    }

    private LeaveRequestDto requestDto(UUID empId, UUID leaveTypeId, BigDecimal days) {
        return new LeaveRequestDto(null, empId, leaveTypeId,
                LocalDate.now(), LocalDate.now().plusDays(days.intValue() - 1),
                days, "Need rest", null, null, null, null);
    }
}
