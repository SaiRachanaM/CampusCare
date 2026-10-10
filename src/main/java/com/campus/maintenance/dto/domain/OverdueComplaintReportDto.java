package com.campus.maintenance.dto.domain;

import java.time.LocalDateTime;

public class OverdueComplaintReportDto {

    private ComplaintResponseDto complaint;
    private long hoursOverdue;

    public OverdueComplaintReportDto() {
    }

    public OverdueComplaintReportDto(ComplaintResponseDto complaint, long hoursOverdue) {
        this.complaint = complaint;
        this.hoursOverdue = hoursOverdue;
    }

    public ComplaintResponseDto getComplaint() {
        return complaint;
    }

    public void setComplaint(ComplaintResponseDto complaint) {
        this.complaint = complaint;
    }

    public long getHoursOverdue() {
        return hoursOverdue;
    }

    public void setHoursOverdue(long hoursOverdue) {
        this.hoursOverdue = hoursOverdue;
    }
}
