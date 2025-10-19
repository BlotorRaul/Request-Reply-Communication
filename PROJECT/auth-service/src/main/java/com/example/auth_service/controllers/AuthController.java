package com.example.auth_service.controllers;

import com.example.auth_service.dtos.AuthUserDTO;
import com.example.auth_service.dtos.LoginRequestDTO;
import com.example.auth_service.dtos.LoginResponseDTO;
import com.example.auth_service.services.AuthUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/users")
public class AuthController {

	private final AuthUserService service;

	public AuthController(AuthUserService service) {
		this.service = service;
	}

	@GetMapping
	public List<AuthUserDTO> getAllUsers() {
		return service.getAllUsers();
	}

	@GetMapping("/{id}")
	public AuthUserDTO getUserById(@PathVariable UUID id) {
		return service.getUserById(id);
	}

	@PostMapping
	public AuthUserDTO createUser(@RequestBody AuthUserDTO dto) {
		return service.createUser(dto);
	}

	@PutMapping("/{id}")
	public AuthUserDTO updateUser(@PathVariable UUID id, @RequestBody AuthUserDTO dto) {
		return service.updateUser(id, dto);
	}

	@DeleteMapping("/{id}")
	public void deleteUser(@PathVariable UUID id) {
		service.deleteUser(id);
	}
	@PostMapping("/login")
	public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
		return service.login(request);
	}
}
