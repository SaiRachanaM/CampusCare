package com.campus.maintenance.entity;

import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint", indexes = {
    @Index(name = "idx_complaint_reported_by", columnList = "reported_by"),
    @Index(name = "idx_complaint_assigned_to", columnList = "assigned_to"),
    @Index(name = "idx_complaint_status", columnList = "status"),
    @Index(name = "idx_complaint_priority", columnList = "priority"),
    @Index(name = "idx_complaint_location", columnList = "location_id"),
    @Index(name = "idx_complaint_asset", columnList = "asset_id")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long complaintId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false, foreignKey = @ForeignKey(name = "fk_complaint_user_reporter"))
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_complaint_location"))
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "asset_id", nullable = true, foreignKey = @ForeignKey(name = "fk_complaint_asset"))
    private Asset asset;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "issue", nullable = false, columnDefinition = "TEXT")
    private String issue;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "priority", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private ComplaintStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_to", nullable = true, foreignKey = @ForeignKey(name = "fk_complaint_user_assigned"))
    private User assignedTo;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    public Complaint() {
    }

    public Complaint(User reportedBy, Location location, Asset asset, String title, String issue, ComplaintPriority priority, ComplaintStatus status) {
        this.reportedBy = reportedBy;
        this.location = location;
        this.asset = asset;
        this.title = title;
        this.issue = issue;
        this.priority = priority;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.reportedAt == null) {
            this.reportedAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
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

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
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
