package com.campus.maintenance.dto.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LocationCreateDto {

    @NotBlank(message = "Building is required")
    @Size(min = 2, max = 100)
    private String building;

    @NotBlank(message = "Floor is required")
    @Size(min = 1, max = 50)
    private String floor;

    @NotBlank(message = "Room is required")
    @Size(min = 1, max = 50)
    private String room;

    public LocationCreateDto() {
    }

    public LocationCreateDto(String building, String floor, String room) {
        this.building = building;
        this.floor = floor;
        this.room = room;
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
}
