package com.example.user_service.services;

import com.example.user_service.dtos.UserDTO;
import com.example.user_service.entities.User;
import com.example.user_service.events.UserEventPublisher;
import com.example.user_service.mappers.UserMapper;
import com.example.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

	private final UserRepository userRepository;
	@Autowired
	private UserEventPublisher userEventPublisher;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	public List<UserDTO> getAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(UserMapper::toDTO)
				.collect(Collectors.toList());
	}

	public UserDTO getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		return UserMapper.toDTO(user);
	}

	/*public UserDTO createUser(UserDTO dto) {
		User user = UserMapper.toEntity(dto);
		User saved = userRepository.save(user);
		return UserMapper.toDTO(saved);
	}*/
	public UserDTO createUser(UserDTO dto) {
		User user = UserMapper.toEntity(dto);
		User saved = userRepository.save(user);
		userEventPublisher.publishUserCreatedEvent(saved.getId().toString(), saved.getEmail());
		return UserMapper.toDTO(saved);
	}

	public UserDTO updateUser(UUID id, UserDTO dto) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));

		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setEmail(dto.getEmail());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setAddress(dto.getAddress());
		user.setCity(dto.getCity());
		user.setCountry(dto.getCountry());
		user.setDepartment(dto.getDepartment());
		user.setJobTitle(dto.getJobTitle());
		user.setActive(dto.getActive());

		User updated = userRepository.save(user);
		return UserMapper.toDTO(updated);
	}

	public void deleteUser(UUID id) {
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("User not found with id: " + id);
		}
		userRepository.deleteById(id);
	}
}
