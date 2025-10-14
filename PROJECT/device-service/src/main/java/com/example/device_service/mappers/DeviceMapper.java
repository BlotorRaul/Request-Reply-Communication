package com.example.device_service.mappers;

import com.example.device_service.dtos.DeviceDTO;
import com.example.device_service.entities.Device;

public class DeviceMapper {

	public static Device toEntity(DeviceDTO dto) {
		Device device = new Device();
		device.setName(dto.getName());
		device.setType(dto.getType());
		device.setStatus(dto.getStatus());
		device.setUserId(dto.getUserId());
		return device;
	}

	public static DeviceDTO toDTO(Device device) {
		DeviceDTO dto = new DeviceDTO();
		dto.setId(device.getId());
		dto.setName(device.getName());
		dto.setType(device.getType());
		dto.setStatus(device.getStatus());
		dto.setUserId(device.getUserId());
		dto.setCreatedAt(device.getCreatedAt());
		dto.setUpdatedAt(device.getUpdatedAt());
		return dto;
	}
}
