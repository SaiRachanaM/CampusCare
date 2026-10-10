package com.campus.maintenance.dto.domain;

import com.campus.maintenance.util.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class ComplaintStatusUpdateDto {

    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    private String remarks;

    public ComplaintStatusUpdateDto() {
    }

    public ComplaintStatusUpdateDto(ComplaintStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
