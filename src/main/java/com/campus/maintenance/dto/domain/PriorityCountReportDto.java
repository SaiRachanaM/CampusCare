package com.campus.maintenance.dto.domain;

import com.campus.maintenance.util.ComplaintPriority;

public class PriorityCountReportDto {

    private ComplaintPriority priority;
    private long count;

    public PriorityCountReportDto() {
    }

    public PriorityCountReportDto(ComplaintPriority priority, long count) {
        this.priority = priority;
        this.count = count;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
