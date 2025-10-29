package com.example.user_service.controllers;

import com.example.user_service.dtos.UserDTO;
import com.example.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing users and publishing events to RabbitMQ")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==========================================================
    @Operation(
            summary = "Create new user",
            description = "Registers a new user in the system and publishes a USER_CREATED event via RabbitMQ.",
            requestBody = @RequestBody(
                    description = "User details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @org.springframework.web.bind.annotation.RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    // ==========================================================
    @Operation(summary = "Get all users", description = "Returns a list of all users stored in the database.")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ==========================================================
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed user information by UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "UUID of the user to retrieve")
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ==========================================================
    @Operation(
            summary = "Update existing user",
            description = "Updates a user's details and publishes a USER_UPDATED event.",
            requestBody = @RequestBody(
                    description = "Updated user data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "UUID of the user to update")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UserDTO dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    // ==========================================================
    @Operation(
            summary = "Delete user",
            description = "Deletes a user by UUID and publishes a USER_DELETED event.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID of the user to delete")
            @PathVariable UUID id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

