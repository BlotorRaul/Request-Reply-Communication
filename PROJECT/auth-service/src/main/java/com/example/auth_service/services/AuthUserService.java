package com.example.auth_service.services;

import com.example.auth_service.dtos.AuthUserDTO;
import com.example.auth_service.entities.AuthUser;
import com.example.auth_service.mappers.AuthUserMapper;
import com.example.auth_service.repositories.AuthUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthUserService {

	private final AuthUserRepository repository;
	private final PasswordEncoder passwordEncoder;

	public AuthUserService(AuthUserRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}


	public List<AuthUserDTO> getAllUsers() {
		return repository.findAll()
				.stream()
				.map(AuthUserMapper::toDTO)
				.collect(Collectors.toList());
	}

	public AuthUserDTO getUserById(UUID id) {
		AuthUser user = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		return AuthUserMapper.toDTO(user);
	}

	public AuthUserDTO createUser(AuthUserDTO dto) {
		AuthUser user = AuthUserMapper.toEntity(dto);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		AuthUser saved = repository.save(user);
		return AuthUserMapper.toDTO(saved);
	}

	public AuthUserDTO updateUser(UUID id, AuthUserDTO dto) {
		AuthUser user = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));

		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(dto.getRole());
		user.setActive(dto.getActive());

		AuthUser updated = repository.save(user);
		return AuthUserMapper.toDTO(updated);
	}

	public void deleteUser(UUID id) {
		if (!repository.existsById(id)) {
			throw new RuntimeException("User not found with id: " + id);
		}
		repository.deleteById(id);
	}
}
