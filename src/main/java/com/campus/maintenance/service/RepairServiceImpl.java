package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.RepairCreateDto;
import com.campus.maintenance.dto.domain.RepairResponseDto;
import com.campus.maintenance.entity.Asset;
import com.campus.maintenance.entity.Complaint;
import com.campus.maintenance.entity.Repair;
import com.campus.maintenance.entity.User;
import com.campus.maintenance.exception.*;
import com.campus.maintenance.repository.AssetRepository;
import com.campus.maintenance.repository.ComplaintRepository;
import com.campus.maintenance.repository.RepairRepository;
import com.campus.maintenance.util.AssetStatus;
import com.campus.maintenance.util.ComplaintStatus;
import com.campus.maintenance.util.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RepairServiceImpl implements RepairService {

    private final RepairRepository repairRepository;
    private final ComplaintRepository complaintRepository;
    private final AssetRepository assetRepository;

    public RepairServiceImpl(
            RepairRepository repairRepository,
            ComplaintRepository complaintRepository,
            AssetRepository assetRepository
    ) {
        this.repairRepository = repairRepository;
        this.complaintRepository = complaintRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    @Transactional
    public RepairResponseDto recordRepair(RepairCreateDto dto, User loggedInStaff) {
        if (loggedInStaff.getRole() != Role.MAINTENANCE_STAFF) {
            throw new AccessDeniedException("Only maintenance staff can record repair logs");
        }

        Complaint complaint = complaintRepository.findById(dto.getComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + dto.getComplaintId()));

        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Cannot log repairs for a closed complaint");
        }

        // 1:1 Relationship Enforced - at most 1 repair record per complaint
        if (repairRepository.existsByComplaint_ComplaintId(dto.getComplaintId())) {
            throw new DuplicateResourceException("A repair record already exists for complaint ID: " + dto.getComplaintId());
        }

        if (dto.getCost() != null && dto.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Repair cost cannot be negative");
        }

        LocalDateTime repairDate = dto.getRepairDate() != null ? dto.getRepairDate() : LocalDateTime.now();

        Repair repair = new Repair(
                complaint,
                repairDate,
                dto.getDescription().trim(),
                dto.getCost(),
                loggedInStaff
        );

        Repair savedRepair = repairRepository.save(repair);

        // Update associated asset status consistently
        if (complaint.getAsset() != null) {
            Asset asset = complaint.getAsset();
            asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
            assetRepository.save(asset);
        }

        return RepairResponseDto.fromEntity(savedRepair);
    }

    @Override
    @Transactional(readOnly = true)
    public RepairResponseDto getRepairByComplaintId(Long complaintId, User currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        if (currentUser.getRole() == Role.USER && !complaint.getReportedBy().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view repairs for another user's complaint");
        }

        Repair repair = repairRepository.findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("No repair record found for complaint ID: " + complaintId));

        return RepairResponseDto.fromEntity(repair);
    }
}
