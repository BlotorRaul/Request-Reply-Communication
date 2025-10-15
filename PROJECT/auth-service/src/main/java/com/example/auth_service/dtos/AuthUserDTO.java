package com.example.auth_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class AuthUserDTO {

	private UUID id;

	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Password is required")
	private String password;

	@NotBlank(message = "Role is required")
	private String role;

	private Boolean active;

	// --- Constructors ---
	public AuthUserDTO() {}

	public AuthUserDTO(UUID id, String email, String password, String role, Boolean active) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.role = role;
		this.active = active;
	}

	// --- Getters & Setters ---
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
