package com.example.device_service.events;

import com.example.device_service.entities.UserLocal;
import com.example.device_service.repositories.DeviceRepository;
import com.example.device_service.repositories.UserLocalRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.device_service.config.RabbitMQConfig.QUEUE;

@Component
public class UserEventConsumer {

    private final UserLocalRepository userRepo;
    private final DeviceRepository deviceRepo;

    public UserEventConsumer(UserLocalRepository userRepo, DeviceRepository deviceRepo) {
        this.userRepo = userRepo;
        this.deviceRepo = deviceRepo;
    }

    @Transactional
    @RabbitListener(queues = QUEUE)
    public void handleUserEvent(Map<String, Object> message) {
        System.out.println("Received user event: " + message);

        String event = (String) message.get("event");
        UUID userId = UUID.fromString((String) message.get("id"));

        switch (event) {
            case "USER_CREATED" -> handleUserCreated(userId, message);
            case "USER_UPDATED" -> handleUserUpdated(userId, message);
            case "USER_DELETED" -> handleUserDeleted(userId);
            default -> System.out.println("Unknown event: " + event);
        }
    }

    @Transactional
    protected void handleUserCreated(UUID id, Map<String, Object> msg) {
        Optional<UserLocal> existing = userRepo.findById(id);

        if (existing.isPresent()) {
            UserLocal user = existing.get();
            user.setFullName((String) msg.get("fullName"));
            user.setEmail((String) msg.get("email"));
            user.setActive((Boolean) msg.get("active"));
            userRepo.saveAndFlush(user);
            System.out.println("User already existed → updated locally: " + user.getFullName());
        } else {
            UserLocal user = new UserLocal();
            user.setId(id);
            user.setFullName((String) msg.get("fullName"));
            user.setEmail((String) msg.get("email"));
            user.setActive((Boolean) msg.get("active"));
            userRepo.saveAndFlush(user);
            System.out.println("User created locally: " + user.getFullName());
        }
    }

    @Transactional
    protected void handleUserUpdated(UUID id, Map<String, Object> msg) {
        userRepo.findById(id).ifPresentOrElse(user -> {
            user.setFullName((String) msg.get("fullName"));
            user.setEmail((String) msg.get("email"));
            user.setActive((Boolean) msg.get("active"));
            userRepo.save(user);
            System.out.println("User updated locally: " + user.getFullName());
        }, () -> {
            System.out.println("Received update for unknown user: " + id);
        });
    }

    @Transactional
    protected void handleUserDeleted(UUID id) {
        userRepo.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepo.save(u);

            // Rupe legaturile din devices
            deviceRepo.findByUserId(id).forEach(d -> {
                d.setUserId(null);
                deviceRepo.save(d);
            });

            System.out.println("User marked inactive and unlinked all devices");
        });
    }
}
