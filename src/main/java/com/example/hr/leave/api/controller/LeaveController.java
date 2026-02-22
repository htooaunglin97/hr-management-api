package com.example.hr.leave.api.controller;

import com.example.hr.leave.dto.LeaveApprovalDto;
import com.example.hr.leave.dto.LeaveRequestDto;
import com.example.hr.leave.service.LeaveService;
import com.example.hr.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for leave management.
 *
 * <p>
 * Deliberately thin — parse HTTP, delegate to {@link LeaveService}, return
 * {@link ApiResponse}.
 * No business logic.
 */
@RestController
@RequestMapping("api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * POST /api/leave
     * Employee applies for leave.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LeaveRequestDto> applyLeave(@Valid @RequestBody LeaveRequestDto request) {
        return ApiResponse.success(leaveService.applyLeave(request), "Leave request submitted");
    }

    /**
     * PUT /api/leave/{requestId}/review
     * Manager approves or rejects a pending leave request.
     */
    @PutMapping("{requestId}/review")
    public ApiResponse<LeaveRequestDto> reviewLeave(
            @PathVariable UUID requestId,
            @Valid @RequestBody LeaveApprovalDto approval) {
        return ApiResponse.success(leaveService.processApproval(requestId, approval), "Leave request reviewed");
    }

    /**
     * PATCH /api/leave/{requestId}/cancel
     * Employee cancels their own pending leave.
     */
    @PatchMapping("{requestId}/cancel")
    public ApiResponse<LeaveRequestDto> cancelLeave(
            @PathVariable UUID requestId,
            @RequestParam UUID employeeId) {
        return ApiResponse.success(leaveService.cancelLeave(requestId, employeeId), "Leave request cancelled");
    }

    /**
     * GET /api/leave/me?employeeId=...&page=0&size=10
     * Employee views their own leave history (paginated).
     */
    @GetMapping("me")
    public ApiResponse<Page<LeaveRequestDto>> myLeaveHistory(
            @RequestParam UUID employeeId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(leaveService.getEmployeeLeaveHistory(employeeId, pageable),
                "Leave history retrieved");
    }

    /**
     * GET /api/leave/pending?page=0&size=10
     * Manager views all pending leave requests (paginated).
     */
    @GetMapping("pending")
    public ApiResponse<Page<LeaveRequestDto>> pendingRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(leaveService.getPendingRequests(pageable), "Pending requests retrieved");
    }
}
