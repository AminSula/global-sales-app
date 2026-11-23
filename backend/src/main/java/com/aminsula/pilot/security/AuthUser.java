package com.aminsula.pilot.security;

import com.aminsula.pilot.user.Role;

public class AuthUser {

    private final Long id;
    private final String username;
    private final String fullName;
    private final Role role;

    public AuthUser(Long id, String username, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
