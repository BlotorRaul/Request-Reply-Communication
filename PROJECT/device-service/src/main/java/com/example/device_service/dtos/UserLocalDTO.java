package com.example.device_service.dtos;

import java.util.UUID;

public class UserLocalDTO {
    private UUID id;
    private String fullName;
    private String email;
    private boolean active;

    public UserLocalDTO() {}

    public UserLocalDTO(UUID id, String fullName, String email, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
