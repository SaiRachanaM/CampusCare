package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.domain.AssetCreateDto;
import com.campus.maintenance.dto.domain.AssetResponseDto;
import com.campus.maintenance.service.AssetService;
import com.campus.maintenance.util.AssetStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AssetResponseDto>>> getAssets(
            @RequestParam(required = false) Long locationId
    ) {
        List<AssetResponseDto> assets;
        if (locationId != null) {
            assets = assetService.getAssetsByLocation(locationId);
        } else {
            assets = assetService.getAllAssets();
        }
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Assets retrieved successfully", assets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AssetResponseDto>> getAssetById(@PathVariable Long id) {
        AssetResponseDto asset = assetService.getAssetById(id);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Asset details retrieved", asset));
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<AssetResponseDto>> createAsset(@Valid @RequestBody AssetCreateDto dto) {
        AssetResponseDto created = assetService.createAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Asset registered successfully", created));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<AssetResponseDto>> updateAssetStatus(
            @PathVariable Long id,
            @RequestParam AssetStatus status
    ) {
        AssetResponseDto updated = assetService.updateAssetStatus(id, status);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Asset status updated successfully", updated));
    }
}
