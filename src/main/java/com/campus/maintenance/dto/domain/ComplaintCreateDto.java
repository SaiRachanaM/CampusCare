package com.campus.maintenance.dto.domain;

import com.campus.maintenance.util.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComplaintCreateDto {

    @NotNull(message = "Location ID is required")
    private Long locationId;

    // Optional asset selection
    private Long assetId;

    @NotBlank(message = "Complaint title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Issue description is required")
    private String issue;

    @NotNull(message = "Priority is required")
    private ComplaintPriority priority;

    public ComplaintCreateDto() {
    }

    public ComplaintCreateDto(Long locationId, Long assetId, String title, String issue, ComplaintPriority priority) {
        this.locationId = locationId;
        this.assetId = assetId;
        this.title = title;
        this.issue = issue;
        this.priority = priority;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
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
}
