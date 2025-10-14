package com.example.device_service.repositories;

import com.example.device_service.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
	List<Device> findByUserId(UUID userId);
}
