package com.example.auth_service.dtos;

import java.util.UUID;

public class LoginResponseDTO {

	private UUID id;
	private String email;
	private String role;
	private boolean active;
	private String message;


	public LoginResponseDTO() {}

	public LoginResponseDTO(UUID id, String email, String role, boolean active, String message) {
		this.id = id;
		this.email = email;
		this.role = role;
		this.active = active;
		this.message = message;
	}

	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
