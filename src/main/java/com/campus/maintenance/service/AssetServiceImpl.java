package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.AssetCreateDto;
import com.campus.maintenance.dto.domain.AssetResponseDto;
import com.campus.maintenance.entity.Asset;
import com.campus.maintenance.entity.Location;
import com.campus.maintenance.exception.ResourceNotFoundException;
import com.campus.maintenance.repository.AssetRepository;
import com.campus.maintenance.repository.LocationRepository;
import com.campus.maintenance.util.AssetStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;

    public AssetServiceImpl(AssetRepository assetRepository, LocationRepository locationRepository) {
        this.assetRepository = assetRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDto> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDto> getAssetsByLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found with ID: " + locationId);
        }
        return assetRepository.findByLocation_LocationId(locationId).stream()
                .map(AssetResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponseDto getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + id));
        return AssetResponseDto.fromEntity(asset);
    }

    @Override
    @Transactional
    public AssetResponseDto createAsset(AssetCreateDto dto) {
        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + dto.getLocationId()));

        AssetStatus status = dto.getStatus() != null ? dto.getStatus() : AssetStatus.FUNCTIONAL;

        Asset asset = new Asset(
                dto.getAssetName().trim(),
                dto.getAssetType().trim(),
                location,
                status
        );

        Asset savedAsset = assetRepository.save(asset);
        return AssetResponseDto.fromEntity(savedAsset);
    }

    @Override
    @Transactional
    public AssetResponseDto updateAssetStatus(Long assetId, AssetStatus status) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));
        asset.setStatus(status);
        Asset updated = assetRepository.save(asset);
        return AssetResponseDto.fromEntity(updated);
    }
}
