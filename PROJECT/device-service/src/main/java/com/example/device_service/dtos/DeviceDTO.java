package com.example.device_service.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeviceDTO {
	private UUID id;
	private String name;
	private String type;
	private String status;
	private UUID userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public DeviceDTO() {}

	public DeviceDTO(UUID id, String name, String type, String status, UUID userId,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.status = status;
		this.userId = userId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public UUID getUserId() { return userId; }
	public void setUserId(UUID userId) { this.userId = userId; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
