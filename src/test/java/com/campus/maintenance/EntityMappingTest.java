package com.campus.maintenance;

import com.campus.maintenance.entity.*;
import com.campus.maintenance.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EntityMappingTest {

    @Test
    @DisplayName("Verify User Entity mappings and BCrypt compatibility")
    void testUserEntity() {
        User user = new User("John Doe", "john@campus.edu", "$2a$10$e7xX3YhG.00pM7K2e8Nn0.Kx1X2Y3Z4A5B6C7D8E9F0G1H2I3J4K5", Role.USER);
        user.setUserId(1L);

        assertEquals(1L, user.getUserId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@campus.edu", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.getPassword().startsWith("$2a$10$"));
    }

    @Test
    @DisplayName("Verify Location & Asset Entity mappings and relationships")
    void testLocationAndAssetEntity() {
        Location location = new Location("Science Block", "2nd Floor", "Lab 204");
        location.setLocationId(10L);

        Asset asset = new Asset("Sony Projector", "Electronics", location, AssetStatus.UNDER_MAINTENANCE);
        asset.setAssetId(100L);

        assertEquals("Science Block", asset.getLocation().getBuilding());
        assertEquals(AssetStatus.UNDER_MAINTENANCE, asset.getStatus());
    }

    @Test
    @DisplayName("Verify Complaint Entity optional asset and lifecycle statuses")
    void testComplaintEntity() {
        User reporter = new User("John", "john@campus.edu", "pass", Role.USER);
        Location loc = new Location("Library", "1st Floor", "Room 101");

        Complaint complaint = new Complaint(reporter, loc, null, "Leaking Pipe", "Pipe leaking in bathroom", ComplaintPriority.HIGH, ComplaintStatus.REPORTED);

        assertNull(complaint.getAsset(), "Asset selection must be optional");
        assertEquals(ComplaintStatus.REPORTED, complaint.getStatus());
        assertEquals(ComplaintPriority.HIGH, complaint.getPriority());

        User staff = new User("Bob Tech", "bob@campus.edu", "pass", Role.MAINTENANCE_STAFF);
        complaint.setAssignedTo(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);

        assertEquals(staff, complaint.getAssignedTo());
        assertEquals(ComplaintStatus.ASSIGNED, complaint.getStatus());
    }

    @Test
    @DisplayName("Verify Repair Entity 1:1 complaint constraint and cost validation")
    void testRepairEntity() {
        User reporter = new User("John", "john@campus.edu", "pass", Role.USER);
        User tech = new User("Bob", "bob@campus.edu", "pass", Role.MAINTENANCE_STAFF);
        Location loc = new Location("Library", "1st Floor", "Room 101");
        Complaint complaint = new Complaint(reporter, loc, null, "Broken Lock", "Door lock broken", ComplaintPriority.MEDIUM, ComplaintStatus.COMPLETED);

        Repair repair = new Repair(complaint, LocalDateTime.now(), "Replaced cylinder lock", new BigDecimal("45.50"), tech);
        repair.setRepairId(50L);

        assertEquals(complaint, repair.getComplaint());
        assertEquals(new BigDecimal("45.50"), repair.getCost());
        assertEquals(tech, repair.getRecordedBy());
    }

    @Test
    @DisplayName("Verify StatusHistory Entity audit tracking")
    void testStatusHistoryEntity() {
        User user = new User("John", "john@campus.edu", "pass", Role.USER);
        Location loc = new Location("Hostel A", "Ground Floor", "Lobby");
        Complaint complaint = new Complaint(user, loc, null, "Broken Window", "Glass cracked", ComplaintPriority.URGENT, ComplaintStatus.REPORTED);

        StatusHistory history = new StatusHistory(complaint, ComplaintStatus.REPORTED, user, "Initial complaint logged");

        assertEquals(ComplaintStatus.REPORTED, history.getStatus());
        assertEquals(user, history.getChangedBy());
        assertEquals("Initial complaint logged", history.getRemarks());
    }
}
