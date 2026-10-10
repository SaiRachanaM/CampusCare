package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.domain.RepairCreateDto;
import com.campus.maintenance.dto.domain.RepairResponseDto;
import com.campus.maintenance.security.CustomUserPrincipal;
import com.campus.maintenance.service.RepairService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<RepairResponseDto>> recordRepair(
            @Valid @RequestBody RepairCreateDto dto,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        RepairResponseDto created = repairService.recordRepair(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Repair work recorded successfully", created));
    }

    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<ApiResponseDto<RepairResponseDto>> getRepairByComplaintId(
            @PathVariable Long complaintId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        RepairResponseDto repair = repairService.getRepairByComplaintId(complaintId, principal.getUser());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Repair details retrieved", repair));
    }
}
