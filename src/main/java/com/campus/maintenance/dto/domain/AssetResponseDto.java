package com.campus.maintenance.dto.domain;

import com.campus.maintenance.entity.Asset;
import com.campus.maintenance.util.AssetStatus;

import java.time.LocalDateTime;

public class AssetResponseDto {

    private Long assetId;
    private String assetName;
    private String assetType;
    private LocationResponseDto location;
    private AssetStatus status;
    private LocalDateTime createdAt;

    public AssetResponseDto() {
    }

    public AssetResponseDto(Long assetId, String assetName, String assetType, LocationResponseDto location, AssetStatus status, LocalDateTime createdAt) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AssetResponseDto fromEntity(Asset asset) {
        if (asset == null) return null;
        return new AssetResponseDto(
                asset.getAssetId(),
                asset.getAssetName(),
                asset.getAssetType(),
                LocationResponseDto.fromEntity(asset.getLocation()),
                asset.getStatus(),
                asset.getCreatedAt()
        );
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

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public LocationResponseDto getLocation() {
        return location;
    }

    public void setLocation(LocationResponseDto location) {
        this.location = location;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
