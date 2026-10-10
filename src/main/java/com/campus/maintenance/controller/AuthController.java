package com.campus.maintenance.controller;

import com.campus.maintenance.dto.ApiResponseDto;
import com.campus.maintenance.dto.auth.*;
import com.campus.maintenance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponseDto response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Authentication successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(false, "Unauthenticated user context", null));
        }
        UserResponseDto user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Current authenticated user profile fetched", user));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getMaintenanceStaff() {
        List<UserResponseDto> staff = authService.getMaintenanceStaff();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Maintenance staff members retrieved", staff));
    }

    @PostMapping("/provision-staff")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> provisionStaff(@Valid @RequestBody StaffProvisionRequest request) {
        UserResponseDto staff = authService.provisionStaffAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, "Maintenance staff account provisioned successfully", staff));
    }
}
