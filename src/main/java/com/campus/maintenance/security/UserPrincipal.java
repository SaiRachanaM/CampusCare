package com.campus.maintenance.security;

import com.campus.maintenance.util.Role;

public class UserPrincipal {

    private final Long userId;
    private final String email;
    private final Role role;

    public UserPrincipal(Long userId, String email, Role role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
