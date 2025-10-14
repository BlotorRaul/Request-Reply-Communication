package com.example.device_service.controllers;

import com.example.device_service.dtos.DeviceDTO;
import com.example.device_service.services.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

	private final DeviceService deviceService;

	public DeviceController(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@PostMapping
	public ResponseEntity<DeviceDTO> createDevice(@RequestBody DeviceDTO dto) {
		return ResponseEntity.ok(deviceService.createDevice(dto));
	}

	@GetMapping
	public ResponseEntity<List<DeviceDTO>> getAllDevices() {
		return ResponseEntity.ok(deviceService.getAllDevices());
	}

	@GetMapping("/{id}")
	public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable UUID id) {
		return ResponseEntity.ok(deviceService.getDeviceById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<DeviceDTO> updateDevice(@PathVariable UUID id, @RequestBody DeviceDTO dto) {
		return ResponseEntity.ok(deviceService.updateDevice(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
		deviceService.deleteDevice(id);
		return ResponseEntity.noContent().build();
	}

	//  Atribuie un device unui user
	@PutMapping("/{id}/assign/{userId}")
	public ResponseEntity<DeviceDTO> assignDeviceToUser(
			@PathVariable UUID id,
			@PathVariable UUID userId) {
		return ResponseEntity.ok(deviceService.assignDeviceToUser(id, userId));
	}

	//  Returneaza device-urile unui anumit user
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@PathVariable UUID userId) {
		return ResponseEntity.ok(deviceService.getDevicesByUser(userId));
	}
}
