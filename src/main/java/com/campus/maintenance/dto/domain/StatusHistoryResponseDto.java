package com.campus.maintenance.dto.domain;

import com.campus.maintenance.dto.auth.UserResponseDto;
import com.campus.maintenance.entity.StatusHistory;
import com.campus.maintenance.util.ComplaintStatus;

import java.time.LocalDateTime;

public class StatusHistoryResponseDto {

    private Long historyId;
    private Long complaintId;
    private ComplaintStatus status;
    private UserResponseDto changedBy;
    private LocalDateTime changedAt;
    private String remarks;

    public StatusHistoryResponseDto() {
    }

    public StatusHistoryResponseDto(Long historyId, Long complaintId, ComplaintStatus status, UserResponseDto changedBy, LocalDateTime changedAt, String remarks) {
        this.historyId = historyId;
        this.complaintId = complaintId;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.remarks = remarks;
    }

    public static StatusHistoryResponseDto fromEntity(StatusHistory history) {
        if (history == null) return null;
        return new StatusHistoryResponseDto(
                history.getHistoryId(),
                history.getComplaint() != null ? history.getComplaint().getComplaintId() : null,
                history.getStatus(),
                UserResponseDto.fromEntity(history.getChangedBy()),
                history.getChangedAt(),
                history.getRemarks()
        );
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public UserResponseDto getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UserResponseDto changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
