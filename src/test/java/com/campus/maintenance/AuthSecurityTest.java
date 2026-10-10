package com.campus.maintenance;

import com.campus.maintenance.dto.auth.LoginRequest;
import com.campus.maintenance.dto.auth.RegisterRequest;
import com.campus.maintenance.dto.auth.StaffProvisionRequest;
import com.campus.maintenance.entity.User;
import com.campus.maintenance.repository.UserRepository;
import com.campus.maintenance.security.JwtService;
import com.campus.maintenance.util.Role;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Successful user registration hashes password with BCrypt and assigns Role.USER")
    void testSuccessfulRegistration() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Test Student", "student@campus.edu", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Test Student")))
                .andExpect(jsonPath("$.data.email", is("student@campus.edu")))
                .andExpect(jsonPath("$.data.role", is("USER")))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        // Verify database persistence and BCrypt password hashing
        User savedUser = userRepository.findByEmail("student@campus.edu").orElse(null);
        assertNotNull(savedUser);
        assertNotEquals("Password123!", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("Password123!", savedUser.getPassword()));
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$"));
        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("2. Public registration ignores client-supplied MAINTENANCE_STAFF role field")
    void testPublicRegistrationCannotCreateStaff() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Attempted Staff", "hacker@campus.edu", "Password123!", "MAINTENANCE_STAFF");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role", is("USER")));

        User savedUser = userRepository.findByEmail("hacker@campus.edu").orElse(null);
        assertNotNull(savedUser);
        assertEquals(Role.USER, savedUser.getRole(), "Role must be strictly set to USER on public registration");
    }

    @Test
    @DisplayName("3. Duplicate email registration rejected with 409 Conflict")
    void testDuplicateEmailRejection() throws Exception {
        User existingUser = new User("Existing User", "existing@campus.edu", passwordEncoder.encode("Password123!"), Role.USER);
        userRepository.save(existingUser);

        RegisterRequest duplicateReq = new RegisterRequest("Duplicate User", "existing@campus.edu", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Email Conflict")))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("4. Validation failure returns 400 Bad Request")
    void testValidationFailure() throws Exception {
        RegisterRequest invalidReq = new RegisterRequest("", "invalid-email-format", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("5. Successful login returns signed JWT token and user info without password")
    void testSuccessfulLogin() throws Exception {
        User user = new User("Login User", "login@campus.edu", passwordEncoder.encode("SecretPass123!"), Role.USER);
        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest("login@campus.edu", "SecretPass123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.email", is("login@campus.edu")))
                .andExpect(jsonPath("$.data.user.role", is("USER")))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    @DisplayName("6. Incorrect password or nonexistent email returns 401 Unauthorized")
    void testInvalidCredentialsRejection() throws Exception {
        User user = new User("User One", "user1@campus.edu", passwordEncoder.encode("CorrectPass!"), Role.USER);
        userRepository.save(user);

        // Case A: Wrong password
        LoginRequest wrongPass = new LoginRequest("user1@campus.edu", "WrongPass!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPass)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));

        // Case B: Non-existent email
        LoginRequest nonExistent = new LoginRequest("nonexistent@campus.edu", "CorrectPass!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistent)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("7. Unauthenticated request to /api/auth/me returns 401 Unauthorized")
    void testUnauthenticatedGetMeRejection() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("8. Authenticated request to /api/auth/me returns current user info")
    void testAuthenticatedGetMeSuccess() throws Exception {
        User user = new User("Auth User", "auth.me@campus.edu", passwordEncoder.encode("Pass123!"), Role.USER);
        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(savedUser);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(savedUser.getUserId().intValue())))
                .andExpect(jsonPath("$.data.email", is("auth.me@campus.edu")))
                .andExpect(jsonPath("$.data.role", is("USER")))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("9. Protected endpoint rejects malformed or invalid JWT with 401 Unauthorized")
    void testInvalidTokenRejection() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. Role-based authorization: USER receives 403 Forbidden accessing STAFF endpoint")
    void testRoleAuthorizationUserDeniedStaffEndpoint() throws Exception {
        User regularUser = new User("Regular Student", "student.role@campus.edu", passwordEncoder.encode("Pass123!"), Role.USER);
        User savedUser = userRepository.save(regularUser);

        String userToken = jwtService.generateToken(savedUser);

        // Accessing /api/test-role/staff requires MAINTENANCE_STAFF
        mockMvc.perform(get("/api/test-role/staff")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("11. Role-based authorization: MAINTENANCE_STAFF succeeds accessing STAFF endpoint")
    void testRoleAuthorizationStaffGrantedStaffEndpoint() throws Exception {
        User staffUser = new User("Bob Tech", "bob.tech@campus.edu", passwordEncoder.encode("Pass123!"), Role.MAINTENANCE_STAFF);
        User savedStaff = userRepository.save(staffUser);

        String staffToken = jwtService.generateToken(savedStaff);

        mockMvc.perform(get("/api/test-role/staff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Staff access granted")));
    }

    @Test
    @DisplayName("12. Staff provisioning succeeds when executed by MAINTENANCE_STAFF")
    void testStaffProvisioningByStaff() throws Exception {
        User staffUser = new User("Sarah Admin", "sarah.admin@campus.edu", passwordEncoder.encode("Pass123!"), Role.MAINTENANCE_STAFF);
        User savedStaff = userRepository.save(staffUser);

        String staffToken = jwtService.generateToken(savedStaff);

        StaffProvisionRequest provisionReq = new StaffProvisionRequest("New Tech", "newtech@campus.edu", "Password123!");

        mockMvc.perform(post("/api/auth/provision-staff")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email", is("newtech@campus.edu")))
                .andExpect(jsonPath("$.data.role", is("MAINTENANCE_STAFF")));

        User createdStaff = userRepository.findByEmail("newtech@campus.edu").orElse(null);
        assertNotNull(createdStaff);
        assertEquals(Role.MAINTENANCE_STAFF, createdStaff.getRole());
    }
}
