package com.example.device_service.mappers;

import com.example.device_service.dtos.UserLocalDTO;
import com.example.device_service.entities.UserLocal;

public class UserLocalMapper {

    public static UserLocal toEntity(UserLocalDTO dto) {
        UserLocal user = new UserLocal();
        user.setId(dto.getId());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setActive(dto.isActive());
        return user;
    }

    public static UserLocalDTO toDTO(UserLocal entity) {
        return new UserLocalDTO(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.isActive()
        );
    }
}
