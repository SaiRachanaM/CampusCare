package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.entity.*;
import com.campus.maintenance.exception.*;
import com.campus.maintenance.repository.*;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;
import com.campus.maintenance.util.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final RepairRepository repairRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public ComplaintServiceImpl(
            ComplaintRepository complaintRepository,
            LocationRepository locationRepository,
            AssetRepository assetRepository,
            UserRepository userRepository,
            RepairRepository repairRepository,
            StatusHistoryRepository statusHistoryRepository
    ) {
        this.complaintRepository = complaintRepository;
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.repairRepository = repairRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    @Transactional
    public ComplaintResponseDto createComplaint(ComplaintCreateDto dto, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required to report complaints");
        }

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + dto.getLocationId()));

        Asset asset = null;
        if (dto.getAssetId() != null) {
            asset = assetRepository.findById(dto.getAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + dto.getAssetId()));

            // Validate that the asset actually belongs to the chosen location
            if (!asset.getLocation().getLocationId().equals(location.getLocationId())) {
                throw new InvalidAssetException("Selected asset (ID: " + asset.getAssetId() + ") does not belong to the selected location (ID: " + location.getLocationId() + ")");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueAt = calculateDueDate(now, dto.getPriority());

        Complaint complaint = new Complaint(
                currentUser,
                location,
                asset,
                dto.getTitle().trim(),
                dto.getIssue().trim(),
                dto.getPriority(),
                ComplaintStatus.REPORTED
        );
        complaint.setDueAt(dueAt);

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Transactional initial status history record
        StatusHistory initialHistory = new StatusHistory(
                savedComplaint,
                ComplaintStatus.REPORTED,
                currentUser,
                "Complaint filed by user: " + currentUser.getName()
        );
        statusHistoryRepository.save(initialHistory);

        return ComplaintResponseDto.fromEntity(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintDetailResponseDto getComplaintById(Long complaintId, User currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        // Ownership & Role Enforcement
        if (currentUser.getRole() == Role.USER && !complaint.getReportedBy().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view another user's complaint");
        }

        Repair repair = repairRepository.findByComplaint_ComplaintId(complaintId).orElse(null);
        List<StatusHistory> historyList = statusHistoryRepository.findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId);

        List<StatusHistoryResponseDto> historyDtos = historyList.stream()
                .map(StatusHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());

        return new ComplaintDetailResponseDto(
                ComplaintResponseDto.fromEntity(complaint),
                RepairResponseDto.fromEntity(repair),
                historyDtos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getComplaintsForUser(User currentUser) {
        return complaintRepository.findByReportedBy_UserIdOrderByReportedAtDesc(currentUser.getUserId()).stream()
                .map(ComplaintResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getAllComplaints(ComplaintStatus status, ComplaintPriority priority, User currentUser) {
        if (currentUser.getRole() != Role.MAINTENANCE_STAFF) {
            throw new AccessDeniedException("Only maintenance staff can view all campus complaints");
        }

        List<Complaint> list;
        if (status != null) {
            list = complaintRepository.findByStatusOrderByReportedAtDesc(status);
        } else if (priority != null) {
            list = complaintRepository.findByPriorityOrderByReportedAtDesc(priority);
        } else {
            list = complaintRepository.findAll();
        }

        return list.stream()
                .map(ComplaintResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponseDto assignComplaint(Long complaintId, ComplaintAssignDto dto, User currentUser) {
        if (currentUser.getRole() != Role.MAINTENANCE_STAFF) {
            throw new AccessDeniedException("Only maintenance staff can assign complaints");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Cannot assign a complaint that is already completed or closed");
        }

        User staff = userRepository.findById(dto.getAssignedToStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with ID: " + dto.getAssignedToStaffId()));

        if (staff.getRole() != Role.MAINTENANCE_STAFF) {
            throw new IllegalArgumentException("Assigned user must have MAINTENANCE_STAFF role");
        }

        complaint.setAssignedTo(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        Complaint updated = complaintRepository.save(complaint);

        String remarks = dto.getRemarks() != null && !dto.getRemarks().isBlank()
                ? dto.getRemarks().trim()
                : "Assigned to staff: " + staff.getName();

        StatusHistory history = new StatusHistory(updated, ComplaintStatus.ASSIGNED, currentUser, remarks);
        statusHistoryRepository.save(history);

        return ComplaintResponseDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public ComplaintResponseDto updateComplaintStatus(Long complaintId, ComplaintStatusUpdateDto dto, User currentUser) {
        if (currentUser.getRole() != Role.MAINTENANCE_STAFF) {
            throw new AccessDeniedException("Only maintenance staff can update complaint status");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        ComplaintStatus currentStatus = complaint.getStatus();
        ComplaintStatus targetStatus = dto.getStatus();

        if (currentStatus == targetStatus) {
            return ComplaintResponseDto.fromEntity(complaint); // Idempotent no-op
        }

        // Validate lifecycle transition rules: REPORTED -> ASSIGNED -> IN_PROGRESS -> COMPLETED -> CLOSED
        validateStatusTransition(currentStatus, targetStatus, complaintId);

        if (targetStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(LocalDateTime.now());
        }

        complaint.setStatus(targetStatus);
        Complaint updated = complaintRepository.save(complaint);

        String remarks = dto.getRemarks() != null ? dto.getRemarks().trim() : "Status updated to " + targetStatus;
        StatusHistory history = new StatusHistory(updated, targetStatus, currentUser, remarks);
        statusHistoryRepository.save(history);

        return ComplaintResponseDto.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusHistoryResponseDto> getComplaintStatusHistory(Long complaintId, User currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        if (currentUser.getRole() == Role.USER && !complaint.getReportedBy().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view status history of another user's complaint");
        }

        return statusHistoryRepository.findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId).stream()
                .map(StatusHistoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateStatusTransition(ComplaintStatus current, ComplaintStatus target, Long complaintId) {
        if (current == ComplaintStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Cannot modify a closed complaint (terminal state)");
        }

        boolean isValid = false;
        switch (current) {
            case REPORTED -> isValid = (target == ComplaintStatus.ASSIGNED || target == ComplaintStatus.IN_PROGRESS);
            case ASSIGNED -> isValid = (target == ComplaintStatus.IN_PROGRESS || target == ComplaintStatus.ASSIGNED);
            case IN_PROGRESS -> isValid = (target == ComplaintStatus.COMPLETED);
            case COMPLETED -> isValid = (target == ComplaintStatus.CLOSED);
            default -> isValid = false;
        }

        if (!isValid) {
            throw new InvalidStatusTransitionException("Invalid complaint status transition from " + current + " to " + target);
        }

        // Prerequisites for COMPLETED or CLOSED: Must have logged a repair record
        if (target == ComplaintStatus.COMPLETED || target == ComplaintStatus.CLOSED) {
            boolean repairExists = repairRepository.existsByComplaint_ComplaintId(complaintId);
            if (!repairExists) {
                throw new InvalidStatusTransitionException("A repair record must be logged before completing or closing a complaint");
            }
        }
    }

    private LocalDateTime calculateDueDate(LocalDateTime reportedAt, ComplaintPriority priority) {
        return switch (priority) {
            case URGENT -> reportedAt.plusDays(1);
            case HIGH -> reportedAt.plusDays(2);
            case MEDIUM -> reportedAt.plusDays(5);
            case LOW -> reportedAt.plusDays(7);
        };
    }
}
