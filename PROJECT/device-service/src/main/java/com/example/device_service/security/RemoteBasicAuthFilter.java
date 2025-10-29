package com.example.device_service.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class RemoteBasicAuthFilter implements Filter {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String authHeader = httpReq.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Trimite cererea la auth-service
            ResponseEntity<Map> resp = restTemplate.exchange(
                    "http://localhost:8083/api/auth/validate-basic",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(resp.getBody().get("valid"))) {

                // Extragem username și rol (opțional)
                String username = (String) resp.getBody().get("username");
                String role = resp.getBody().get("roles") != null
                        ? resp.getBody().get("roles").toString()
                        : "USER";

                // Construim obiectul de autentificare
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singleton(new SimpleGrantedAuthority(role))
                        );

                // Setam in contextul de securitate
                SecurityContextHolder.getContext().setAuthentication(authentication);

                chain.doFilter(request, response);
            } else {
                httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            }

        } catch (Exception e) {
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }
    }
}
