package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.*;

import java.util.List;

public interface ReportService {
    List<ComplaintResponseDto> getPendingComplaints();
    List<OverdueComplaintReportDto> getOverdueComplaints();
    List<AssetFailureReportDto> getFrequentlyFailingAssets();
    List<StatusCountReportDto> getComplaintCountsByStatus();
    List<PriorityCountReportDto> getComplaintCountsByPriority();
    RepairCostSummaryDto getRepairCostSummary();
}
