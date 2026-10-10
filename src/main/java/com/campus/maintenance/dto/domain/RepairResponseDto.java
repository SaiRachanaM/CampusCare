package com.campus.maintenance.dto.domain;

import com.campus.maintenance.dto.auth.UserResponseDto;
import com.campus.maintenance.entity.Repair;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairResponseDto {

    private Long repairId;
    private Long complaintId;
    private LocalDateTime repairDate;
    private String description;
    private BigDecimal cost;
    private UserResponseDto recordedBy;
    private LocalDateTime createdAt;

    public RepairResponseDto() {
    }

    public RepairResponseDto(Long repairId, Long complaintId, LocalDateTime repairDate, String description, BigDecimal cost, UserResponseDto recordedBy, LocalDateTime createdAt) {
        this.repairId = repairId;
        this.complaintId = complaintId;
        this.repairDate = repairDate;
        this.description = description;
        this.cost = cost;
        this.recordedBy = recordedBy;
        this.createdAt = createdAt;
    }

    public static RepairResponseDto fromEntity(Repair repair) {
        if (repair == null) return null;
        return new RepairResponseDto(
                repair.getRepairId(),
                repair.getComplaint() != null ? repair.getComplaint().getComplaintId() : null,
                repair.getRepairDate(),
                repair.getDescription(),
                repair.getCost(),
                UserResponseDto.fromEntity(repair.getRecordedBy()),
                repair.getCreatedAt()
        );
    }

    public Long getRepairId() {
        return repairId;
    }

    public void setRepairId(Long repairId) {
        this.repairId = repairId;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public LocalDateTime getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(LocalDateTime repairDate) {
        this.repairDate = repairDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public UserResponseDto getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UserResponseDto recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
