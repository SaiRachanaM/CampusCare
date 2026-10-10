package com.campus.maintenance.dto.domain;

import jakarta.validation.constraints.NotNull;

public class ComplaintAssignDto {

    @NotNull(message = "Target staff ID is required")
    private Long assignedToStaffId;

    private String remarks;

    public ComplaintAssignDto() {
    }

    public ComplaintAssignDto(Long assignedToStaffId, String remarks) {
        this.assignedToStaffId = assignedToStaffId;
        this.remarks = remarks;
    }

    public Long getAssignedToStaffId() {
        return assignedToStaffId;
    }

    public void setAssignedToStaffId(Long assignedToStaffId) {
        this.assignedToStaffId = assignedToStaffId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
