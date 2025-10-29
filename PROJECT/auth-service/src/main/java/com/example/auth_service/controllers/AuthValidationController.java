package com.example.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.auth_service.security.CustomUserDetailsService;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/validate-basic")
public class AuthValidationController {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthValidationController(CustomUserDetailsService userDetailsService,
                                    AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> validateUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Missing Authorization header"));
            }

            // Decode Basic Base64
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);

            String username = values[0];
            String password = values[1];

            // Autentifica in baza de date
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            if (auth.isAuthenticated()) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                System.out.println("Basic Auth validated for user: " + username);

                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", user.getUsername(),
                        "roles", user.getAuthorities()
                ));
            }

            return ResponseEntity.status(401).body(Map.of("valid", false));

        } catch (Exception e) {
            System.out.println("Auth validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
