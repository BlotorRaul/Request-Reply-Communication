package com.example.device_service.controllers;

import com.example.device_service.dtos.UserLocalDTO;
import com.example.device_service.services.UserLocalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users-local")
@Tag(name = "Local Users", description = "Endpoints for managing local cached users received via RabbitMQ")
public class UserLocalController {

    private final UserLocalService service;

    public UserLocalController(UserLocalService service) {
        this.service = service;
    }

    @Operation(summary = "Get all local users", description = "Returns all users stored locally in device-service.")
    @GetMapping
    public ResponseEntity<List<UserLocalDTO>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @Operation(summary = "Get local user by ID", description = "Retrieves local user details by UUID.")
    @GetMapping("/{id}")
    public ResponseEntity<UserLocalDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @Operation(summary = "Create local user manually", description = "Creates a local user entry (usually for testing).")
    @PostMapping
    public ResponseEntity<UserLocalDTO> createUser(@org.springframework.web.bind.annotation.RequestBody UserLocalDTO dto) {
        return ResponseEntity.ok(service.createUser(dto));
    }

    @Operation(summary = "Update local user", description = "Updates a user’s local cached data.")
    @PutMapping("/{id}")
    public ResponseEntity<UserLocalDTO> updateUser(@PathVariable UUID id, @org.springframework.web.bind.annotation.RequestBody UserLocalDTO dto) {
        return ResponseEntity.ok(service.updateUser(id, dto));
    }

    @Operation(summary = "Delete local user", description = "Deletes a user from local cache.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate local user", description = "Marks a user as inactive and simulates USER_DELETED propagation.")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        service.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
