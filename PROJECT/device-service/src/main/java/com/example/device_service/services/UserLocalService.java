package com.example.device_service.services;

import com.example.device_service.dtos.UserLocalDTO;
import com.example.device_service.entities.UserLocal;
import com.example.device_service.mappers.UserLocalMapper;
import com.example.device_service.repositories.UserLocalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserLocalService {

    private final UserLocalRepository repository;

    public UserLocalService(UserLocalRepository repository) {
        this.repository = repository;
    }

    public List<UserLocalDTO> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(UserLocalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserLocalDTO getUserById(UUID id) {
        UserLocal user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return UserLocalMapper.toDTO(user);
    }

    public UserLocalDTO createUser(UserLocalDTO dto) {
        UserLocal user = UserLocalMapper.toEntity(dto);
        return UserLocalMapper.toDTO(repository.save(user));
    }

    public UserLocalDTO updateUser(UUID id, UserLocalDTO dto) {
        UserLocal user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setActive(dto.isActive());

        return UserLocalMapper.toDTO(repository.save(user));
    }

    public void deleteUser(UUID id) {
        repository.deleteById(id);
    }

    // “Simulare” propagare manuala a stergerii din user-service
    public void deactivateUser(UUID id) {
        UserLocal user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setActive(false);
        repository.save(user);
    }
}
