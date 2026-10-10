package com.campus.maintenance.dto.domain;

import java.math.BigDecimal;

public class RepairCostSummaryDto {

    private BigDecimal totalCost;
    private BigDecimal averageCost;
    private long totalRepairs;

    public RepairCostSummaryDto() {
    }

    public RepairCostSummaryDto(BigDecimal totalCost, BigDecimal averageCost, long totalRepairs) {
        this.totalCost = totalCost;
        this.averageCost = averageCost;
        this.totalRepairs = totalRepairs;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public long getTotalRepairs() {
        return totalRepairs;
    }

    public void setTotalRepairs(long totalRepairs) {
        this.totalRepairs = totalRepairs;
    }
}
