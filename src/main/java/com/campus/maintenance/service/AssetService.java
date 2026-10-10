package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.AssetCreateDto;
import com.campus.maintenance.dto.domain.AssetResponseDto;
import com.campus.maintenance.util.AssetStatus;

import java.util.List;

public interface AssetService {
    List<AssetResponseDto> getAllAssets();
    List<AssetResponseDto> getAssetsByLocation(Long locationId);
    AssetResponseDto getAssetById(Long id);
    AssetResponseDto createAsset(AssetCreateDto dto);
    AssetResponseDto updateAssetStatus(Long assetId, AssetStatus status);
}
