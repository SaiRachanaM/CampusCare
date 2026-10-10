package com.campus.maintenance.dto.domain;

public class AssetFailureReportDto {

    private Long assetId;
    private String assetName;
    private String building;
    private long failureCount;

    public AssetFailureReportDto() {
    }

    public AssetFailureReportDto(Long assetId, String assetName, String building, long failureCount) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.building = building;
        this.failureCount = failureCount;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }
}
