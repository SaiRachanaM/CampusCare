package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.domain.LocationCreateDto;
import com.campus.maintenance.dto.domain.LocationResponseDto;
import com.campus.maintenance.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<LocationResponseDto>>> getAllLocations() {
        List<LocationResponseDto> locations = locationService.getAllLocations();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Campus locations retrieved successfully", locations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<LocationResponseDto>> getLocationById(@PathVariable Long id) {
        LocationResponseDto location = locationService.getLocationById(id);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Location details retrieved", location));
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<LocationResponseDto>> createLocation(@Valid @RequestBody LocationCreateDto dto) {
        LocationResponseDto created = locationService.createLocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Location created successfully", created));
    }
}
