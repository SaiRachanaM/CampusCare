package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('MAINTENANCE_STAFF')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponseDto<List<ComplaintResponseDto>>> getPendingComplaints() {
        List<ComplaintResponseDto> report = reportService.getPendingComplaints();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Pending complaints report generated", report));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponseDto<List<OverdueComplaintReportDto>>> getOverdueComplaints() {
        List<OverdueComplaintReportDto> report = reportService.getOverdueComplaints();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Overdue complaints report generated", report));
    }

    @GetMapping("/frequently-failing-assets")
    public ResponseEntity<ApiResponseDto<List<AssetFailureReportDto>>> getFrequentlyFailingAssets() {
        List<AssetFailureReportDto> report = reportService.getFrequentlyFailingAssets();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Frequently failing assets report generated", report));
    }

    @GetMapping("/counts-by-status")
    public ResponseEntity<ApiResponseDto<List<StatusCountReportDto>>> getComplaintCountsByStatus() {
        List<StatusCountReportDto> report = reportService.getComplaintCountsByStatus();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint counts by status report generated", report));
    }

    @GetMapping("/counts-by-priority")
    public ResponseEntity<ApiResponseDto<List<PriorityCountReportDto>>> getComplaintCountsByPriority() {
        List<PriorityCountReportDto> report = reportService.getComplaintCountsByPriority();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Complaint counts by priority report generated", report));
    }

    @GetMapping("/repair-costs")
    public ResponseEntity<ApiResponseDto<RepairCostSummaryDto>> getRepairCostSummary() {
        RepairCostSummaryDto report = reportService.getRepairCostSummary();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Repair cost summary report generated", report));
    }
}
