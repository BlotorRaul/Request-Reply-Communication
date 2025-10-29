package com.example.device_service.services;

import com.example.device_service.dtos.DeviceDTO;
import com.example.device_service.entities.Device;
import com.example.device_service.entities.UserLocal;
import com.example.device_service.mappers.DeviceMapper;
import com.example.device_service.repositories.DeviceRepository;
import com.example.device_service.repositories.UserLocalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserLocalRepository userLocalRepository;

    public DeviceService(DeviceRepository deviceRepository, UserLocalRepository userLocalRepository) {
        this.deviceRepository = deviceRepository;
        this.userLocalRepository = userLocalRepository;
    }

	public List<DeviceDTO> getAllDevices() {
		return deviceRepository.findAll()
				.stream()
				.map(DeviceMapper::toDTO)
				.collect(Collectors.toList());
	}

	public DeviceDTO getDeviceById(UUID id) {
		Device device = deviceRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found: " + id));
		return DeviceMapper.toDTO(device);
	}

	public DeviceDTO createDevice(DeviceDTO dto) {
		Device device = DeviceMapper.toEntity(dto);
		Device saved = deviceRepository.save(device);
		return DeviceMapper.toDTO(saved);
	}

	public DeviceDTO updateDevice(UUID id, DeviceDTO dto) {
		Device device = deviceRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found: " + id));

		device.setName(dto.getName());
		device.setType(dto.getType());
		device.setStatus(dto.getStatus());
		device.setUserId(dto.getUserId());

		Device updated = deviceRepository.save(device);
		return DeviceMapper.toDTO(updated);
	}

	public void deleteDevice(UUID id) {
		if (!deviceRepository.existsById(id))
			throw new RuntimeException("Device not found: " + id);
		deviceRepository.deleteById(id);
	}


	//  Atribuie un device unui user
	public DeviceDTO assignDeviceToUser(UUID id, UUID userId) {
		Device device = deviceRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

		device.setUserId(userId);
		Device updated = deviceRepository.save(device);

		return DeviceMapper.toDTO(updated);
	}

	//  Returneaza device-urile unui anumit user
	public List<DeviceDTO> getDevicesByUser(UUID userId) {
		return deviceRepository.findByUserId(userId)
				.stream()
				.map(DeviceMapper::toDTO)
				.collect(Collectors.toList());
	}
    public DeviceDTO assignUserToDevice(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        // Verifica dacă userul exista in users_local
        UserLocal user = userLocalRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found locally: " + userId));

        // Daca userul este inactiv, aruncam eroare controlata
        if (!user.isActive()) {
            throw new IllegalStateException("User " + user.getFullName() + " is inactive and cannot be assigned a device.");
        }

        // Asociem device-ul
        device.setUserId(userId);
        Device updated = deviceRepository.save(device);

        System.out.println("Device " + device.getName() + " assigned to active user " + user.getFullName());
        return DeviceMapper.toDTO(updated);
    }


    public DeviceDTO unassignUserFromDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        device.setUserId(null);
        Device updated = deviceRepository.save(device);
        System.out.println("Device " + device.getName() + " unassigned from user");

        return DeviceMapper.toDTO(updated);
    }

}
