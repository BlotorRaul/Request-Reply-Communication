package com.example.user_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserDTO {

	private UUID id; // se va popula automat la GET/response

	@NotBlank(message = "First name is required")
	private String firstName;

	@NotBlank(message = "Last name is required")
	private String lastName;

	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	private String phoneNumber;
	private String address;
	private String city;
	private String country;
	private String department;
	private String jobTitle;
	private Boolean active;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// ===========================
	//        Constructors
	// ===========================

	public UserDTO() {
	}

	public UserDTO(UUID id, String firstName, String lastName, String email,
			String phoneNumber, String address, String city,
			String country, String department, String jobTitle,
			Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.city = city;
		this.country = country;
		this.department = department;
		this.jobTitle = jobTitle;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// ===========================
	//        Getters / Setters
	// ===========================

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// ===========================
	//        Utility methods
	// ===========================

	@Override
	public String toString() {
		return "UserDTO{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", email='" + email + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", address='" + address + '\'' +
				", city='" + city + '\'' +
				", country='" + country + '\'' +
				", department='" + department + '\'' +
				", jobTitle='" + jobTitle + '\'' +
				", active=" + active +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserDTO)) return false;
		UserDTO userDTO = (UserDTO) o;
		return Objects.equals(id, userDTO.id) &&
				Objects.equals(email, userDTO.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, email);
	}
}
