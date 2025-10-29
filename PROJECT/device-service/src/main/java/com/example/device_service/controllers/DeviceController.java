package com.example.device_service.controllers;

import com.example.device_service.dtos.DeviceDTO;
import com.example.device_service.services.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Endpoints for managing devices and user-device assignments")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(
            summary = "Create new device",
            description = "Registers a new device in the system.",
            requestBody = @RequestBody(
                    description = "Device details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Device created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@org.springframework.web.bind.annotation.RequestBody DeviceDTO dto) {
        return ResponseEntity.ok(deviceService.createDevice(dto));
    }

    @Operation(summary = "Get all devices", description = "Returns a list of all devices stored in the database.")
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @Operation(summary = "Get device by ID", description = "Retrieves detailed information about a device by its UUID.")
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(
            @Parameter(description = "UUID of the device") @PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @Operation(summary = "Update device", description = "Updates name, type, status, or assigned user of a device.")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @Parameter(description = "UUID of the device") @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody DeviceDTO dto) {
        return ResponseEntity.ok(deviceService.updateDevice(id, dto));
    }

    @Operation(summary = "Delete device", description = "Deletes a device from the system.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@Parameter(description = "UUID of the device") @PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign device to user", description = "Links a device to a specific user ID.")
    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<DeviceDTO> assignDeviceToUser(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.assignDeviceToUser(id, userId));
    }

    @Operation(summary = "Get all devices for user", description = "Returns all devices assigned to a given user.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.getDevicesByUser(userId));
    }

    @Operation(summary = "Assign user to device (with validation)", description = "Assigns an active user to a device. Fails if user is inactive or missing.")
    @PutMapping("/{deviceId}/assign-user/{userId}")
    public ResponseEntity<?> assignUserToDevice(@PathVariable UUID deviceId, @PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(deviceService.assignUserToDevice(deviceId, userId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Unassign user from device", description = "Removes the user-device link.")
    @PutMapping("/{deviceId}/unassign-user")
    public ResponseEntity<DeviceDTO> unassignUserFromDevice(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(deviceService.unassignUserFromDevice(deviceId));
    }
}
