package com.example.auth_service.config;

import com.example.auth_service.entities.AuthUser;
import com.example.auth_service.repositories.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AuthUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== DataInitializer STARTED ===");
        try {
            createAdminIfNotExists();
            createUserIfNotExists();
            System.out.println("=== DataInitializer COMPLETED ===");
        } catch (Exception e) {
            System.err.println("Error initializing default users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createAdminIfNotExists() {
        System.out.println("Checking for admin@example.com...");
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            System.out.println("Admin user not found, creating...");
            AuthUser admin = new AuthUser();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            userRepository.saveAndFlush(admin);
            System.out.println("Created default admin user: admin@example.com");
        } else {
            System.out.println("Admin user already exists");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createUserIfNotExists() {
        System.out.println("Checking for user@example.com...");
        if (userRepository.findByEmail("user@example.com").isEmpty()) {
            System.out.println("User not found, creating...");
            AuthUser user = new AuthUser();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            user.setActive(true);
            userRepository.saveAndFlush(user);
            System.out.println("Created default user: user@example.com");
        } else {
            System.out.println("User already exists");
        }
    }
}

