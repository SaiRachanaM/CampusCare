package com.campus.maintenance.dto.domain;

import com.campus.maintenance.util.ComplaintStatus;

public class StatusCountReportDto {

    private ComplaintStatus status;
    private long count;

    public StatusCountReportDto() {
    }

    public StatusCountReportDto(ComplaintStatus status, long count) {
        this.status = status;
        this.count = count;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
