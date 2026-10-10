package com.campus.maintenance.service;

import com.campus.maintenance.dto.auth.*;
import com.campus.maintenance.entity.User;
import com.campus.maintenance.exception.EmailAlreadyExistsException;
import com.campus.maintenance.exception.InvalidCredentialsException;
import com.campus.maintenance.exception.ResourceNotFoundException;
import com.campus.maintenance.repository.UserRepository;
import com.campus.maintenance.security.JwtService;
import com.campus.maintenance.util.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                Role.USER
        );

        User savedUser = userRepository.save(user);
        return UserResponseDto.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, jwtService.getJwtExpirationMs(), UserResponseDto.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserResponseDto.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponseDto provisionStaffAccount(StaffProvisionRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Staff email address is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User staff = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                Role.MAINTENANCE_STAFF
        );

        User savedStaff = userRepository.save(staff);
        return UserResponseDto.fromEntity(savedStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getMaintenanceStaff() {
        return userRepository.findByRole(Role.MAINTENANCE_STAFF).stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
