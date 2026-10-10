package com.campus.maintenance.dto.domain;

import com.campus.maintenance.dto.auth.UserResponseDto;
import com.campus.maintenance.entity.Complaint;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;

import java.time.LocalDateTime;

public class ComplaintResponseDto {

    private Long complaintId;
    private UserResponseDto reportedBy;
    private LocationResponseDto location;
    private AssetResponseDto asset;
    private String title;
    private String issue;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private UserResponseDto assignedTo;
    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private LocalDateTime dueAt;

    public ComplaintResponseDto() {
    }

    public ComplaintResponseDto(Long complaintId, UserResponseDto reportedBy, LocationResponseDto location, AssetResponseDto asset, String title, String issue, ComplaintPriority priority, ComplaintStatus status, UserResponseDto assignedTo, LocalDateTime reportedAt, LocalDateTime updatedAt, LocalDateTime closedAt, LocalDateTime dueAt) {
        this.complaintId = complaintId;
        this.reportedBy = reportedBy;
        this.location = location;
        this.asset = asset;
        this.title = title;
        this.issue = issue;
        this.priority = priority;
        this.status = status;
        this.assignedTo = assignedTo;
        this.reportedAt = reportedAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.dueAt = dueAt;
    }

    public static ComplaintResponseDto fromEntity(Complaint complaint) {
        if (complaint == null) return null;
        return new ComplaintResponseDto(
                complaint.getComplaintId(),
                UserResponseDto.fromEntity(complaint.getReportedBy()),
                LocationResponseDto.fromEntity(complaint.getLocation()),
                AssetResponseDto.fromEntity(complaint.getAsset()),
                complaint.getTitle(),
                complaint.getIssue(),
                complaint.getPriority(),
                complaint.getStatus(),
                UserResponseDto.fromEntity(complaint.getAssignedTo()),
                complaint.getReportedAt(),
                complaint.getUpdatedAt(),
                complaint.getClosedAt(),
                complaint.getDueAt()
        );
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public UserResponseDto getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(UserResponseDto reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocationResponseDto getLocation() {
        return location;
    }

    public void setLocation(LocationResponseDto location) {
        this.location = location;
    }

    public AssetResponseDto getAsset() {
        return asset;
    }

    public void setAsset(AssetResponseDto asset) {
        this.asset = asset;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public UserResponseDto getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserResponseDto assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }
}
