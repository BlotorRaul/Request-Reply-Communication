package com.example.auth_service.controllers;

import com.example.auth_service.dtos.AuthUserDTO;
import com.example.auth_service.dtos.LoginRequestDTO;
import com.example.auth_service.dtos.LoginResponseDTO;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.services.AuthUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/users")
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication, registration, and management."
)
public class AuthController {

    private final AuthUserService service;
    private final JwtUtil jwtUtil;

    public AuthController(AuthUserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }


    @Operation(
            summary = "Get all users",
            description = "Returns a list of all registered users in the authentication system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
            }
    )
    @GetMapping
    public List<AuthUserDTO> getAllUsers() {
        return service.getAllUsers();
    }


    @Operation(
            summary = "Get user by ID",
            description = "Fetches the user details for a given UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public AuthUserDTO getUserById(
            @Parameter(
                    description = "UUID of the user to retrieve",
                    example = "2e8b25f4-83d2-47e6-b8e2-84d8297b0f66"
            )
            @PathVariable UUID id
    ) {
        return service.getUserById(id);
    }


    @Operation(
            summary = "Create a new user",
            description = "Registers a new user with email, password, role, and status.",
            requestBody = @RequestBody(
                    required = true,
                    description = "User details for registration",
                    content = @Content(schema = @Schema(implementation = AuthUserDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public AuthUserDTO createUser(@org.springframework.web.bind.annotation.RequestBody AuthUserDTO dto) {
        return service.createUser(dto);
    }


    @Operation(
            summary = "Update user",
            description = "Updates an existing user's email, password, role, or active status.",
            requestBody = @RequestBody(
                    description = "Updated user information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthUserDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PutMapping("/{id}")
    public AuthUserDTO updateUser(
            @Parameter(description = "UUID of the user to update")
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody AuthUserDTO dto
    ) {
        return service.updateUser(id, dto);
    }


    @Operation(
            summary = "Delete user",
            description = "Deletes a user from the database using their UUID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{id}")
    public void deleteUser(
            @Parameter(description = "UUID of the user to delete")
            @PathVariable UUID id
    ) {
        service.deleteUser(id);
    }


    @Operation(
            summary = "Authenticate user",
            description = "Validates the user's credentials and returns a JWT token on success.",
            requestBody = @RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
                            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public LoginResponseDTO login(@org.springframework.web.bind.annotation.RequestBody LoginRequestDTO request) {
        return service.login(request);
    }

    @Operation(
            summary = "Validate JWT token",
            description = "Checks if a given JWT token is valid and not expired."
    )
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam String token
    ) {
        boolean valid = jwtUtil.validateToken(token);
        Map<String, Object> response = Map.of(
                "valid", valid,
                "token", token,
                "message", valid ? "Token is valid" : "Token is invalid or expired"
        );
        return ResponseEntity.ok(response);
    }




    @Operation(
            summary = "Test endpoint for USER role",
            description = "Checks if a logged-in user with ROLE_USER can access this route.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access granted for USER role"),
                    @ApiResponse(responseCode = "403", description = "Access denied for non-USER roles")
            }
    )
    @GetMapping("/test-user")
    public String testUserAccess() {
        return " Hello USER! You have access to this endpoint.";
    }
}
