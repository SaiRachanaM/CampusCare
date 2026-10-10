package com.campus.maintenance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test-role")
public class TestProtectedController {

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> userEndpoint() {
        return ResponseEntity.ok(Map.of("message", "User access granted"));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<Map<String, String>> staffEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Staff access granted"));
    }
}
