package com.campus.maintenance.dto.domain;

import java.util.ArrayList;
import java.util.List;

public class ComplaintDetailResponseDto {

    private ComplaintResponseDto complaint;
    private RepairResponseDto repair;
    private List<StatusHistoryResponseDto> history = new ArrayList<>();

    public ComplaintDetailResponseDto() {
    }

    public ComplaintDetailResponseDto(ComplaintResponseDto complaint, RepairResponseDto repair, List<StatusHistoryResponseDto> history) {
        this.complaint = complaint;
        this.repair = repair;
        if (history != null) {
            this.history = history;
        }
    }

    public ComplaintResponseDto getComplaint() {
        return complaint;
    }

    public void setComplaint(ComplaintResponseDto complaint) {
        this.complaint = complaint;
    }

    public RepairResponseDto getRepair() {
        return repair;
    }

    public void setRepair(RepairResponseDto repair) {
        this.repair = repair;
    }

    public List<StatusHistoryResponseDto> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistoryResponseDto> history) {
        this.history = history;
    }
}
