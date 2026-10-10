package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.security.CustomUserPrincipal;
import com.campus.maintenance.service.ComplaintService;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<ComplaintResponseDto>> createComplaint(
            @Valid @RequestBody ComplaintCreateDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ComplaintResponseDto created = complaintService.createComplaint(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Complaint reported successfully", created));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<List<ComplaintResponseDto>>> getMyComplaints(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ComplaintResponseDto> myComplaints = complaintService.getComplaintsForUser(principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Your reported complaints retrieved", myComplaints));
    }

    @GetMapping
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<List<ComplaintResponseDto>>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ComplaintResponseDto> complaints = complaintService.getAllComplaints(status, priority, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "All campus complaints retrieved", complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ComplaintDetailResponseDto>> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ComplaintDetailResponseDto detail = complaintService.getComplaintById(id, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint details retrieved", detail));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<ComplaintResponseDto>> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintAssignDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ComplaintResponseDto updated = complaintService.assignComplaint(id, dto, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint assigned successfully", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<ComplaintResponseDto>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ComplaintResponseDto updated = complaintService.updateComplaintStatus(id, dto, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint status updated successfully", updated));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponseDto<List<StatusHistoryResponseDto>>> getComplaintHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<StatusHistoryResponseDto> history = complaintService.getComplaintStatusHistory(id, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint status history timeline retrieved", history));
    }
}
