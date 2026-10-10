package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.LocationCreateDto;
import com.campus.maintenance.dto.domain.LocationResponseDto;

import java.util.List;

public interface LocationService {
    List<LocationResponseDto> getAllLocations();
    LocationResponseDto getLocationById(Long id);
    LocationResponseDto createLocation(LocationCreateDto dto);
}
