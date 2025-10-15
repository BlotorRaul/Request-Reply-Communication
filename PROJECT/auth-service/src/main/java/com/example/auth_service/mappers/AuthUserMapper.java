package com.example.auth_service.mappers;

import com.example.auth_service.dtos.AuthUserDTO;
import com.example.auth_service.entities.AuthUser;

public class AuthUserMapper {

	//  DTO -> Entity
	public static AuthUser toEntity(AuthUserDTO dto) {
		AuthUser user = new AuthUser();
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword());
		user.setRole(dto.getRole());
		user.setActive(dto.getActive() != null ? dto.getActive() : true);
		return user;
	}

	//  Entity -> DTO
	public static AuthUserDTO toDTO(AuthUser entity) {
		return new AuthUserDTO(
				entity.getId(),
				entity.getEmail(),
				null, // parola nu o expunem
				entity.getRole(),
				entity.isActive()
		);
	}
}
