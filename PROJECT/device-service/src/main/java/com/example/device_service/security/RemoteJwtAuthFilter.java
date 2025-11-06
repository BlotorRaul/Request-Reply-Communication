package com.example.device_service.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class RemoteJwtAuthFilter implements Filter {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${AUTH_SERVICE_URL:http://localhost:8083}")
    private String authServiceUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        
        // Permitem cererile pentru Swagger fara autentificare
        String path = httpReq.getRequestURI();
        if (path != null && (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs"))) {
            chain.doFilter(request, response);
            return;
        }
        
        String authHeader = httpReq.getHeader("Authorization");
        System.out.println("[RemoteJwtAuthFilter] Authorization header received: " + (authHeader == null ? "null" : (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader)));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT token (JWT filter)");
            return;
        }

        try {
            // Trimite tokenul catre auth-service pentru validare
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    authServiceUrl + "/api/auth/users/validate-jwt",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = resp.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("valid"))) {
                String username = (String) body.get("username");
                String role = (String) body.getOrDefault("role", "USER");

                // Creeaza contextul de autentificare
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                chain.doFilter(request, response);
            } else {
                httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            }

        } catch (Exception e) {
            e.printStackTrace();
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT validation failed: " + e.getMessage());
        }
    }
}
