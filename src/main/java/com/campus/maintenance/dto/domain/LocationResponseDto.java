package com.campus.maintenance.dto.domain;

import com.campus.maintenance.entity.Location;

import java.time.LocalDateTime;

public class LocationResponseDto {

    private Long locationId;
    private String building;
    private String floor;
    private String room;
    private LocalDateTime createdAt;

    public LocationResponseDto() {
    }

    public LocationResponseDto(Long locationId, String building, String floor, String room, LocalDateTime createdAt) {
        this.locationId = locationId;
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.createdAt = createdAt;
    }

    public static LocationResponseDto fromEntity(Location location) {
        if (location == null) return null;
        return new LocationResponseDto(
                location.getLocationId(),
                location.getBuilding(),
                location.getFloor(),
                location.getRoom(),
                location.getCreatedAt()
        );
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
