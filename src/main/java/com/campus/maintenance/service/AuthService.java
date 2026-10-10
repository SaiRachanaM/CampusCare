package com.campus.maintenance.service;

import com.campus.maintenance.dto.auth.*;

import java.util.List;

public interface AuthService {
    UserResponseDto registerUser(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponseDto getCurrentUser(String email);
    UserResponseDto provisionStaffAccount(StaffProvisionRequest request);
    List<UserResponseDto> getMaintenanceStaff();
}
