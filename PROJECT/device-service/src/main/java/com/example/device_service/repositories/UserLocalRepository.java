package com.example.device_service.repositories;

import com.example.device_service.entities.UserLocal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLocalRepository extends JpaRepository<UserLocal, UUID> {
}
