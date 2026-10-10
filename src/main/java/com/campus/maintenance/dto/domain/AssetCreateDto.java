package com.campus.maintenance.dto.domain;

import com.campus.maintenance.util.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssetCreateDto {

    @NotBlank(message = "Asset name is required")
    @Size(min = 2, max = 150)
    private String assetName;

    @NotBlank(message = "Asset type is required")
    @Size(min = 2, max = 100)
    private String assetType;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    private AssetStatus status = AssetStatus.FUNCTIONAL;

    public AssetCreateDto() {
    }

    public AssetCreateDto(String assetName, String assetType, Long locationId, AssetStatus status) {
        this.assetName = assetName;
        this.assetType = assetType;
        this.locationId = locationId;
        if (status != null) {
            this.status = status;
        }
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }
}
