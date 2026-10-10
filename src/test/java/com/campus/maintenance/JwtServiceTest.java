package com.campus.maintenance;

import com.campus.maintenance.entity.User;
import com.campus.maintenance.exception.JwtAuthenticationException;
import com.campus.maintenance.security.JwtService;
import com.campus.maintenance.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice Smith", "alice.jwt@campus.edu", "encoded_pass", Role.USER);
        sampleUser.setUserId(42L);
    }

    @Test
    @DisplayName("Generate valid JWT and extract subject, claims, and role")
    void testTokenGenerationAndExtraction() {
        String token = jwtService.generateToken(sampleUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("alice.jwt@campus.edu", jwtService.extractSubject(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, "alice.jwt@campus.edu"));
    }

    @Test
    @DisplayName("Reject malformed or tampered JWT token")
    void testMalformedJwtRejection() {
        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidSignature";

        assertThrows(JwtAuthenticationException.class, () -> jwtService.extractAllClaims(malformedToken));
        assertFalse(jwtService.isTokenValid(malformedToken, "alice.jwt@campus.edu"));
    }

    @Test
    @DisplayName("Reject expired JWT token")
    void testExpiredJwtRejection() {
        JwtService shortLivedJwtService = new JwtService();
        ReflectionTestUtils.setField(shortLivedJwtService, "secretKey", "CampusMaintenanceSecretKeyForJWTSigning1234567890TestKeyOnly");
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtExpirationMs", -1000L); // Expired 1 sec ago

        String expiredToken = shortLivedJwtService.generateToken(sampleUser);

        assertThrows(JwtAuthenticationException.class, () -> shortLivedJwtService.extractAllClaims(expiredToken));
        assertFalse(shortLivedJwtService.isTokenValid(expiredToken, "alice.jwt@campus.edu"));
    }

    @Test
    @DisplayName("Reject JWT with invalid signature key")
    void testInvalidSignatureJwtRejection() {
        JwtService foreignJwtService = new JwtService();
        ReflectionTestUtils.setField(foreignJwtService, "secretKey", "DifferentForeignSecretKeyForJWTSigning1234567890Different");
        ReflectionTestUtils.setField(foreignJwtService, "jwtExpirationMs", 3600000L);

        String foreignToken = foreignJwtService.generateToken(sampleUser);

        // System's configured jwtService uses a different secret key
        assertThrows(JwtAuthenticationException.class, () -> jwtService.extractAllClaims(foreignToken));
    }
}
