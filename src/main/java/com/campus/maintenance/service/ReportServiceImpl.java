package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.entity.Complaint;
import com.campus.maintenance.repository.ComplaintRepository;
import com.campus.maintenance.repository.RepairRepository;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ComplaintRepository complaintRepository;
    private final RepairRepository repairRepository;

    public ReportServiceImpl(ComplaintRepository complaintRepository, RepairRepository repairRepository) {
        this.complaintRepository = complaintRepository;
        this.repairRepository = repairRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getPendingComplaints() {
        List<ComplaintStatus> pendingStatuses = List.of(
                ComplaintStatus.REPORTED,
                ComplaintStatus.ASSIGNED,
                ComplaintStatus.IN_PROGRESS
        );
        return complaintRepository.findByStatusInOrderByReportedAtDesc(pendingStatuses).stream()
                .map(ComplaintResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverdueComplaintReportDto> getOverdueComplaints() {
        LocalDateTime now = LocalDateTime.now();
        List<ComplaintStatus> terminalStatuses = List.of(ComplaintStatus.COMPLETED, ComplaintStatus.CLOSED);

        List<Complaint> overdueList = complaintRepository.findOverdueComplaints(terminalStatuses, now);

        List<OverdueComplaintReportDto> reports = new ArrayList<>();
        for (Complaint complaint : overdueList) {
            long hoursOverdue = 0;
            if (complaint.getDueAt() != null) {
                hoursOverdue = Math.max(0, Duration.between(complaint.getDueAt(), now).toHours());
            }
            reports.add(new OverdueComplaintReportDto(ComplaintResponseDto.fromEntity(complaint), hoursOverdue));
        }

        return reports;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetFailureReportDto> getFrequentlyFailingAssets() {
        List<Object[]> rawResults = complaintRepository.findFrequentlyFailingAssets();
        List<AssetFailureReportDto> dtos = new ArrayList<>();

        for (Object[] row : rawResults) {
            Long assetId = (Long) row[0];
            String assetName = (String) row[1];
            String building = (String) row[2];
            long count = ((Number) row[3]).longValue();
            dtos.add(new AssetFailureReportDto(assetId, assetName, building, count));
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusCountReportDto> getComplaintCountsByStatus() {
        List<Object[]> rawResults = complaintRepository.countComplaintsGroupByStatus();
        List<StatusCountReportDto> dtos = new ArrayList<>();

        for (Object[] row : rawResults) {
            ComplaintStatus status = (ComplaintStatus) row[0];
            long count = ((Number) row[1]).longValue();
            dtos.add(new StatusCountReportDto(status, count));
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriorityCountReportDto> getComplaintCountsByPriority() {
        List<Object[]> rawResults = complaintRepository.countComplaintsGroupByPriority();
        List<PriorityCountReportDto> dtos = new ArrayList<>();

        for (Object[] row : rawResults) {
            ComplaintPriority priority = (ComplaintPriority) row[0];
            long count = ((Number) row[1]).longValue();
            dtos.add(new PriorityCountReportDto(priority, count));
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public RepairCostSummaryDto getRepairCostSummary() {
        Object rawResult = repairRepository.getRepairCostSummary();
        if (rawResult == null) {
            return new RepairCostSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        Object[] row;
        if (rawResult instanceof Object[][] matrix && matrix.length > 0) {
            row = matrix[0];
        } else if (rawResult instanceof Object[] arr && arr.length > 0) {
            if (arr[0] instanceof Object[] nestedRow) {
                row = nestedRow;
            } else {
                row = arr;
            }
        } else {
            return new RepairCostSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        BigDecimal totalCost = parseBigDecimal(row.length > 0 ? row[0] : null);
        BigDecimal averageCost = parseBigDecimal(row.length > 1 ? row[1] : null);
        long totalRepairs = 0;
        if (row.length > 2 && row[2] instanceof Number num) {
            totalRepairs = num.longValue();
        }

        return new RepairCostSummaryDto(totalCost, averageCost, totalRepairs);
    }

    private BigDecimal parseBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (val instanceof BigDecimal bd) return bd.setScale(2, RoundingMode.HALF_UP);
        if (val instanceof Number num) return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        try {
            return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
