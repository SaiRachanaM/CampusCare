package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.LocationCreateDto;
import com.campus.maintenance.dto.domain.LocationResponseDto;
import com.campus.maintenance.entity.Location;
import com.campus.maintenance.exception.DuplicateResourceException;
import com.campus.maintenance.exception.ResourceNotFoundException;
import com.campus.maintenance.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponseDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(LocationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponseDto getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
        return LocationResponseDto.fromEntity(location);
    }

    @Override
    @Transactional
    public LocationResponseDto createLocation(LocationCreateDto dto) {
        String building = dto.getBuilding().trim();
        String floor = dto.getFloor().trim();
        String room = dto.getRoom().trim();

        if (locationRepository.findByBuildingAndFloorAndRoom(building, floor, room).isPresent()) {
            throw new DuplicateResourceException("Location already exists for building: " + building + ", floor: " + floor + ", room: " + room);
        }

        Location location = new Location(building, floor, room);
        Location savedLocation = locationRepository.save(location);
        return LocationResponseDto.fromEntity(savedLocation);
    }
}
