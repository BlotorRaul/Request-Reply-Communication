package com.example.user_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RemoteAuthModeProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    // poți suprascrie din environment: AUTH_SERVICE_URL=http://auth-service:8083
    @Value("${AUTH_SERVICE_URL:http://localhost:8083}")
    private String authServiceUrl;

    public String getAuthMode() {
        try {
            String url = authServiceUrl + "/api/auth/users/mode";
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            return response.getOrDefault("mode", "default").toLowerCase();
        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch auth mode from auth-service (" + e.getMessage() + "). Defaulting to 'default'");
            return "default";
        }
    }
}
