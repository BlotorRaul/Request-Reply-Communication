package com.example.user_service.mappers;

import com.example.user_service.dtos.UserDTO;
import com.example.user_service.entities.User;

public class UserMapper {

	public static User toEntity(UserDTO dto) {
		if (dto == null) {
			return null;
		}

		User user = new User();
		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setEmail(dto.getEmail());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setAddress(dto.getAddress());
		user.setCity(dto.getCity());
		user.setCountry(dto.getCountry());
		user.setDepartment(dto.getDepartment());
		user.setJobTitle(dto.getJobTitle());
		user.setActive(dto.getActive() != null ? dto.getActive() : true);
		return user;
	}

	public static UserDTO toDTO(User user) {
		if (user == null) {
			return null;
		}

		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setFirstName(user.getFirstName());
		dto.setLastName(user.getLastName());
		dto.setEmail(user.getEmail());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setAddress(user.getAddress());
		dto.setCity(user.getCity());
		dto.setCountry(user.getCountry());
		dto.setDepartment(user.getDepartment());
		dto.setJobTitle(user.getJobTitle());
		dto.setActive(user.getActive());
		dto.setCreatedAt(user.getCreatedAt());
		dto.setUpdatedAt(user.getUpdatedAt());
		return dto;
	}
}
