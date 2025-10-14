package com.example.user_service.controllers;

import com.example.user_service.dtos.UserDTO;
import com.example.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")

public class UserController {

	@NonNull
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}
	// CREATE
	@PostMapping
	public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
		return ResponseEntity.ok(userService.createUser(dto));
	}

	// READ ALL
	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	// READ BY ID
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.getUserById(id));
	}

	// UPDATE
	@PutMapping("/{id}")
	public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDTO dto) {
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	// DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
