package com.campus.maintenance.dto.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairCreateDto {

    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    @NotBlank(message = "Repair description is required")
    private String description;

    @NotNull(message = "Repair cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Repair cost cannot be negative")
    private BigDecimal cost;

    private LocalDateTime repairDate;

    public RepairCreateDto() {
    }

    public RepairCreateDto(Long complaintId, String description, BigDecimal cost) {
        this.complaintId = complaintId;
        this.description = description;
        this.cost = cost;
    }

    public RepairCreateDto(Long complaintId, String description, BigDecimal cost, LocalDateTime repairDate) {
        this.complaintId = complaintId;
        this.description = description;
        this.cost = cost;
        this.repairDate = repairDate;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
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

    public LocalDateTime getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(LocalDateTime repairDate) {
        this.repairDate = repairDate;
    }
}
