package com.campus.maintenance;

import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.entity.*;
import com.campus.maintenance.repository.*;
import com.campus.maintenance.security.JwtService;
import com.campus.maintenance.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User student1;
    private User student2;
    private User staff1;
    private String student1Token;
    private String student2Token;
    private String staff1Token;
    private Location loc1;
    private Asset asset1;

    @BeforeEach
    void setUp() {
        repairRepository.deleteAll();
        complaintRepository.deleteAll();
        assetRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();

        student1 = userRepository.save(new User("John Student", "john.student@campus.edu", passwordEncoder.encode("Pass123!"), Role.USER));
        student2 = userRepository.save(new User("Mary Student", "mary.student@campus.edu", passwordEncoder.encode("Pass123!"), Role.USER));
        staff1 = userRepository.save(new User("Bob Tech", "bob.tech@campus.edu", passwordEncoder.encode("Pass123!"), Role.MAINTENANCE_STAFF));

        student1Token = jwtService.generateToken(student1);
        student2Token = jwtService.generateToken(student2);
        staff1Token = jwtService.generateToken(staff1);

        loc1 = locationRepository.save(new Location("Science Block", "3rd Floor", "Lab 301"));
        asset1 = assetRepository.save(new Asset("Optoma Projector", "Electronics", loc1, AssetStatus.FUNCTIONAL));
    }

    @Test
    @DisplayName("1. Unauthenticated requests to protected endpoints are rejected with 401 Unauthorized")
    void testUnauthenticatedRequestsRejected() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/pending"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/repairs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. USER can retrieve locations, assets, submit complaints, and view their own complaints")
    void testUserAuthorizedEndpoints() throws Exception {
        // GET /api/locations
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        // GET /api/assets
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // POST /api/complaints
        ComplaintCreateDto dto = new ComplaintCreateDto(loc1.getLocationId(), asset1.getAssetId(), "Projector Overheating", "Shuts down after 15 min", ComplaintPriority.HIGH);
        mockMvc.perform(post("/api/complaints")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("REPORTED")))
                .andExpect(jsonPath("$.data.reportedBy.email", is("john.student@campus.edu")));

        // GET /api/complaints/my
        mockMvc.perform(get("/api/complaints/my")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("3. USER is forbidden from listing all complaints (403 Forbidden)")
    void testUserForbiddenFromAllComplaints() throws Exception {
        mockMvc.perform(get("/api/complaints")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("4. USER is forbidden from creating locations, assets, repairs, and viewing reports")
    void testUserForbiddenFromManagementActions() throws Exception {
        // Location Creation
        LocationCreateDto locDto = new LocationCreateDto("New Block", "1st Floor", "Room 101");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locDto)))
                .andExpect(status().isForbidden());

        // Asset Creation
        AssetCreateDto assetDto = new AssetCreateDto("New Router", "Networking", loc1.getLocationId(), AssetStatus.FUNCTIONAL);
        mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetDto)))
                .andExpect(status().isForbidden());

        // Reports Access
        mockMvc.perform(get("/api/reports/pending")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. USER is forbidden from viewing another user's complaint")
    void testUserForbiddenFromOtherUserComplaint() throws Exception {
        Complaint c1 = complaintRepository.save(new Complaint(student1, loc1, asset1, "Student 1 Issue", "Desc", ComplaintPriority.MEDIUM, ComplaintStatus.REPORTED));

        // Student 2 attempts to view Student 1's complaint details
        mockMvc.perform(get("/api/complaints/" + c1.getComplaintId())
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. MAINTENANCE_STAFF can access management endpoints successfully")
    void testStaffManagementEndpoints() throws Exception {
        // Create Location
        LocationCreateDto locDto = new LocationCreateDto("Admin Block", "2nd Floor", "Conf Hall");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + staff1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.building", is("Admin Block")));

        // View All Complaints
        mockMvc.perform(get("/api/complaints")
                        .header("Authorization", "Bearer " + staff1Token))
                .andExpect(status().isOk());

        // View Reports
        mockMvc.perform(get("/api/reports/repair-costs")
                        .header("Authorization", "Bearer " + staff1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("7. Nonexistent resources return 404 Not Found")
    void testNonexistentResourceReturns404() throws Exception {
        mockMvc.perform(get("/api/locations/99999")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        mockMvc.perform(get("/api/complaints/99999")
                        .header("Authorization", "Bearer " + staff1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("8. Validation failures return 400 Bad Request")
    void testValidationFailureReturns400() throws Exception {
        // Blank location fields
        LocationCreateDto invalidLoc = new LocationCreateDto("", "", "");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + staff1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoc)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        // Negative repair cost
        RepairCreateDto invalidRepair = new RepairCreateDto(1L, "Description", new BigDecimal("-50.00"));
        mockMvc.perform(post("/api/repairs")
                        .header("Authorization", "Bearer " + staff1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRepair)))
                .andExpect(status().isBadRequest());
    }
}
