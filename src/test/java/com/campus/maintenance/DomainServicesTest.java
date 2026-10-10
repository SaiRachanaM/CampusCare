package com.campus.maintenance;

import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.entity.*;
import com.campus.maintenance.exception.*;
import com.campus.maintenance.repository.*;
import com.campus.maintenance.service.*;
import com.campus.maintenance.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DomainServicesTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private RepairService repairService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    private User student1;
    private User student2;
    private User staff1;
    private Location locBlockA;
    private Location locBlockB;
    private Asset assetProjector;

    @BeforeEach
    void setUp() {
        repairRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        assetRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();

        student1 = userRepository.save(new User("John Student", "john@campus.edu", "pass", Role.USER));
        student2 = userRepository.save(new User("Mary Student", "mary@campus.edu", "pass", Role.USER));
        staff1 = userRepository.save(new User("Bob Technician", "bob.staff@campus.edu", "pass", Role.MAINTENANCE_STAFF));

        locBlockA = locationRepository.save(new Location("Science Block", "1st Floor", "Lab 101"));
        locBlockB = locationRepository.save(new Location("Library Block", "Ground Floor", "Reading Room"));

        assetProjector = assetRepository.save(new Asset("Sony HD Projector", "Electronics", locBlockA, AssetStatus.FUNCTIONAL));
    }

    @Test
    @DisplayName("1. Complaint creation with and without optional asset selection")
    void testComplaintCreationWithAndWithoutAsset() {
        // Without Asset
        ComplaintCreateDto dtoNoAsset = new ComplaintCreateDto(locBlockA.getLocationId(), null, "Leaking Sink", "Water dripping from faucet", ComplaintPriority.MEDIUM);
        ComplaintResponseDto response1 = complaintService.createComplaint(dtoNoAsset, student1);

        assertNotNull(response1.getComplaintId());
        assertNull(response1.getAsset());
        assertEquals(ComplaintStatus.REPORTED, response1.getStatus());
        assertEquals("John Student", response1.getReportedBy().getName());

        // With Asset
        ComplaintCreateDto dtoWithAsset = new ComplaintCreateDto(locBlockA.getLocationId(), assetProjector.getAssetId(), "Projector Lamp Issue", "Lamp warning light red", ComplaintPriority.HIGH);
        ComplaintResponseDto response2 = complaintService.createComplaint(dtoWithAsset, student1);

        assertNotNull(response2.getAsset());
        assertEquals("Sony HD Projector", response2.getAsset().getAssetName());
    }

    @Test
    @DisplayName("2. Rejection of invalid location and mismatched asset location")
    void testRejectionOfInvalidLocationAndMismatchedAsset() {
        // Invalid location ID
        ComplaintCreateDto invalidLocDto = new ComplaintCreateDto(9999L, null, "Broken Table", "Table leg split", ComplaintPriority.LOW);
        assertThrows(ResourceNotFoundException.class, () -> complaintService.createComplaint(invalidLocDto, student1));

        // Mismatched Asset: assetProjector belongs to locBlockA, but complaint specifies locBlockB
        ComplaintCreateDto mismatchedAssetDto = new ComplaintCreateDto(locBlockB.getLocationId(), assetProjector.getAssetId(), "Mismatched Complaint", "Testing mismatch", ComplaintPriority.HIGH);
        assertThrows(InvalidAssetException.class, () -> complaintService.createComplaint(mismatchedAssetDto, student1));
    }

    @Test
    @DisplayName("3. Ownership restrictions: User cannot access another user's complaint")
    void testOwnershipRestrictions() {
        ComplaintCreateDto dto = new ComplaintCreateDto(locBlockA.getLocationId(), null, "Door Handle Loose", "Handle falling off", ComplaintPriority.LOW);
        ComplaintResponseDto complaint = complaintService.createComplaint(dto, student1);

        // Student 1 (reporter) can access
        assertDoesNotThrow(() -> complaintService.getComplaintById(complaint.getComplaintId(), student1));

        // Staff 1 can access any complaint
        assertDoesNotThrow(() -> complaintService.getComplaintById(complaint.getComplaintId(), staff1));

        // Student 2 (unrelated user) is denied access
        assertThrows(AccessDeniedException.class, () -> complaintService.getComplaintById(complaint.getComplaintId(), student2));
    }

    @Test
    @DisplayName("4. Valid and invalid complaint assignments")
    void testComplaintAssignments() {
        ComplaintCreateDto dto = new ComplaintCreateDto(locBlockA.getLocationId(), null, "AC Not Cooling", "Temperature hot", ComplaintPriority.HIGH);
        ComplaintResponseDto complaint = complaintService.createComplaint(dto, student1);

        // Regular user attempting assignment -> AccessDeniedException
        ComplaintAssignDto assignDto = new ComplaintAssignDto(staff1.getUserId(), "Assigning to staff");
        assertThrows(AccessDeniedException.class, () -> complaintService.assignComplaint(complaint.getComplaintId(), assignDto, student1));

        // Assigning to a non-staff user (student2) -> IllegalArgumentException
        ComplaintAssignDto invalidStaffAssign = new ComplaintAssignDto(student2.getUserId(), "Assigning to student");
        assertThrows(IllegalArgumentException.class, () -> complaintService.assignComplaint(complaint.getComplaintId(), invalidStaffAssign, staff1));

        // Valid assignment by staff1 to staff1
        ComplaintResponseDto assignedComplaint = complaintService.assignComplaint(complaint.getComplaintId(), assignDto, staff1);
        assertEquals(ComplaintStatus.ASSIGNED, assignedComplaint.getStatus());
        assertEquals("Bob Technician", assignedComplaint.getAssignedTo().getName());
    }

    @Test
    @DisplayName("5. Valid and invalid status transitions and terminal state enforcement")
    void testStatusTransitionsAndLifecycle() {
        ComplaintCreateDto dto = new ComplaintCreateDto(locBlockA.getLocationId(), assetProjector.getAssetId(), "Power Outlet Spark", "Outlet sparks when plugged", ComplaintPriority.URGENT);
        ComplaintResponseDto complaint = complaintService.createComplaint(dto, student1);
        Long complaintId = complaint.getComplaintId();

        // REPORTED -> CLOSED directly -> InvalidStatusTransitionException (No skipping steps)
        assertThrows(InvalidStatusTransitionException.class, () ->
                complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.CLOSED, "Direct close"), staff1)
        );

        // REPORTED -> ASSIGNED
        complaintService.assignComplaint(complaintId, new ComplaintAssignDto(staff1.getUserId(), "Assigned"), staff1);

        // ASSIGNED -> IN_PROGRESS
        complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.IN_PROGRESS, "Working on it"), staff1);

        // IN_PROGRESS -> COMPLETED without repair record -> Rejected!
        assertThrows(InvalidStatusTransitionException.class, () ->
                complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.COMPLETED, "Done"), staff1)
        );

        // Record Repair record
        RepairCreateDto repairDto = new RepairCreateDto(complaintId, "Replaced socket wiring", new BigDecimal("75.00"));
        repairService.recordRepair(repairDto, staff1);

        // IN_PROGRESS -> COMPLETED (now valid after repair record logged)
        ComplaintResponseDto completed = complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.COMPLETED, "Work complete"), staff1);
        assertEquals(ComplaintStatus.COMPLETED, completed.getStatus());

        // COMPLETED -> CLOSED
        ComplaintResponseDto closed = complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.CLOSED, "Ticket closed"), staff1);
        assertEquals(ComplaintStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());

        // CLOSED -> IN_PROGRESS (Terminal state rejection)
        assertThrows(InvalidStatusTransitionException.class, () ->
                complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.IN_PROGRESS, "Reopen attempt"), staff1)
        );
    }

    @Test
    @DisplayName("6. Status history creation and audit timeline tracking")
    void testStatusHistoryTracking() {
        ComplaintCreateDto dto = new ComplaintCreateDto(locBlockA.getLocationId(), null, "Fan Noise", "Ceiling fan making squeaking noise", ComplaintPriority.LOW);
        ComplaintResponseDto complaint = complaintService.createComplaint(dto, student1);
        Long complaintId = complaint.getComplaintId();

        complaintService.assignComplaint(complaintId, new ComplaintAssignDto(staff1.getUserId(), "Staff assigned"), staff1);

        List<StatusHistoryResponseDto> history = complaintService.getComplaintStatusHistory(complaintId, student1);
        assertEquals(2, history.size());
        assertEquals(ComplaintStatus.REPORTED, history.get(0).getStatus());
        assertEquals(ComplaintStatus.ASSIGNED, history.get(1).getStatus());
    }

    @Test
    @DisplayName("7. Repair validation (cost >= 0) and 1:1 duplicate repair prevention")
    void testRepairValidationAndDuplicatePrevention() {
        ComplaintCreateDto dto = new ComplaintCreateDto(locBlockA.getLocationId(), assetProjector.getAssetId(), "Focus Motor Jammed", "Projector lens stuck", ComplaintPriority.MEDIUM);
        ComplaintResponseDto complaint = complaintService.createComplaint(dto, student1);
        Long complaintId = complaint.getComplaintId();

        complaintService.assignComplaint(complaintId, new ComplaintAssignDto(staff1.getUserId(), "Assigned"), staff1);
        complaintService.updateComplaintStatus(complaintId, new ComplaintStatusUpdateDto(ComplaintStatus.IN_PROGRESS, "Started repair"), staff1);

        // Negative repair cost -> Rejected
        RepairCreateDto negativeCostDto = new RepairCreateDto(complaintId, "Fixed focus gear", new BigDecimal("-10.00"));
        assertThrows(IllegalArgumentException.class, () -> repairService.recordRepair(negativeCostDto, staff1));

        // Valid Repair record
        RepairCreateDto validRepair = new RepairCreateDto(complaintId, "Lubricated and aligned focus gear", new BigDecimal("120.00"));
        RepairResponseDto repairResp = repairService.recordRepair(validRepair, staff1);
        assertNotNull(repairResp.getRepairId());
        assertEquals(new BigDecimal("120.00"), repairResp.getCost());

        // Second repair for same complaint -> DuplicateResourceException (1:1 constraint)
        RepairCreateDto duplicateRepair = new RepairCreateDto(complaintId, "Second repair attempt", new BigDecimal("50.00"));
        assertThrows(DuplicateResourceException.class, () -> repairService.recordRepair(duplicateRepair, staff1));
    }

    @Test
    @DisplayName("8. Overdue complaint calculations")
    void testOverdueComplaintCalculations() {
        Complaint complaint = new Complaint(student1, locBlockA, null, "Overdue Water Leak", "Pipe leaking for days", ComplaintPriority.URGENT, ComplaintStatus.REPORTED);
        complaint.setDueAt(LocalDateTime.now().minusHours(48)); // Overdue by 48 hours
        complaintRepository.save(complaint);

        List<OverdueComplaintReportDto> overdueReports = reportService.getOverdueComplaints();
        assertFalse(overdueReports.isEmpty());

        boolean found = overdueReports.stream().anyMatch(r -> r.getComplaint().getComplaintId().equals(complaint.getComplaintId()) && r.getHoursOverdue() >= 47);
        assertTrue(found, "Overdue complaint calculation must correctly report overdue hours");
    }

    @Test
    @DisplayName("9. Frequently failing assets analytics report")
    void testFrequentlyFailingAssetsReport() {
        // Create 2 complaints for assetProjector
        ComplaintCreateDto dto1 = new ComplaintCreateDto(locBlockA.getLocationId(), assetProjector.getAssetId(), "Fail 1", "Issue 1", ComplaintPriority.LOW);
        ComplaintCreateDto dto2 = new ComplaintCreateDto(locBlockA.getLocationId(), assetProjector.getAssetId(), "Fail 2", "Issue 2", ComplaintPriority.MEDIUM);

        complaintService.createComplaint(dto1, student1);
        complaintService.createComplaint(dto2, student1);

        List<AssetFailureReportDto> failureReports = reportService.getFrequentlyFailingAssets();
        assertFalse(failureReports.isEmpty());

        AssetFailureReportDto topFailing = failureReports.get(0);
        assertEquals("Sony HD Projector", topFailing.getAssetName());
        assertEquals(2, topFailing.getFailureCount());
    }

    @Test
    @DisplayName("10. Complaint counts by status and priority reports")
    void testComplaintCountsByStatusAndPriority() {
        complaintService.createComplaint(new ComplaintCreateDto(locBlockA.getLocationId(), null, "Issue 1", "Desc 1", ComplaintPriority.HIGH), student1);
        complaintService.createComplaint(new ComplaintCreateDto(locBlockA.getLocationId(), null, "Issue 2", "Desc 2", ComplaintPriority.LOW), student1);

        List<StatusCountReportDto> statusCounts = reportService.getComplaintCountsByStatus();
        List<PriorityCountReportDto> priorityCounts = reportService.getComplaintCountsByPriority();

        assertFalse(statusCounts.isEmpty());
        assertFalse(priorityCounts.isEmpty());
    }

    @Test
    @DisplayName("11. Repair cost summary calculations")
    void testRepairCostSummary() {
        Complaint c1 = complaintRepository.save(new Complaint(student1, locBlockA, null, "Fix 1", "Desc 1", ComplaintPriority.MEDIUM, ComplaintStatus.IN_PROGRESS));
        Complaint c2 = complaintRepository.save(new Complaint(student1, locBlockB, null, "Fix 2", "Desc 2", ComplaintPriority.HIGH, ComplaintStatus.IN_PROGRESS));

        repairService.recordRepair(new RepairCreateDto(c1.getComplaintId(), "Repair 1", new BigDecimal("100.00")), staff1);
        repairService.recordRepair(new RepairCreateDto(c2.getComplaintId(), "Repair 2", new BigDecimal("200.00")), staff1);

        RepairCostSummaryDto summary = reportService.getRepairCostSummary();
        assertEquals(new BigDecimal("300.00"), summary.getTotalCost());
        assertEquals(new BigDecimal("150.00"), summary.getAverageCost());
        assertEquals(2, summary.getTotalRepairs());
    }

    @Test
    @DisplayName("12. Location and Asset duplicate prevention")
    void testLocationAndAssetDuplicatePrevention() {
        LocationCreateDto duplicateLoc = new LocationCreateDto("Science Block", "1st Floor", "Lab 101");
        assertThrows(DuplicateResourceException.class, () -> locationService.createLocation(duplicateLoc));

        AssetCreateDto assetDto = new AssetCreateDto("Smartboard", "Electronics", locBlockA.getLocationId(), AssetStatus.FUNCTIONAL);
        AssetResponseDto newAsset = assetService.createAsset(assetDto);
        assertNotNull(newAsset.getAssetId());
    }
}
