package com.example.device_service.services;

import com.example.device_service.dtos.DeviceDTO;
import com.example.device_service.entities.Device;
import com.example.device_service.mappers.DeviceMapper;
import com.example.device_service.repositories.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

	private final DeviceRepository deviceRepository;

	public DeviceService(DeviceRepository deviceRepository) {
		this.deviceRepository = deviceRepository;
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
}
