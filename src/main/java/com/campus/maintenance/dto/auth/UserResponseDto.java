package com.campus.maintenance.dto.auth;

import com.campus.maintenance.entity.User;
import com.campus.maintenance.util.Role;

import java.time.LocalDateTime;

public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String name, String email, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UserResponseDto fromEntity(User user) {
        if (user == null) return null;
        return new UserResponseDto(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
