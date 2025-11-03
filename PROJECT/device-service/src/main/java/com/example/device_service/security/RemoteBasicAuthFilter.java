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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RemoteBasicAuthFilter implements Filter {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${AUTH_SERVICE_URL:http://localhost:8083}")
    private String authServiceUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String authHeader = httpReq.getHeader("Authorization");
        System.out.println("[RemoteBasicAuthFilter] Authorization header received: " + (authHeader == null ? "null" : (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader)));

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header (BASIC filter)");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Trimite cererea la auth-service
            ResponseEntity<Map> resp = restTemplate.exchange(
                    authServiceUrl + "/api/auth/validate-basic",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(resp.getBody().get("valid"))) {

                String username = (String) resp.getBody().get("username");

                //  Extragem lista reala de roluri din auth-service
                Object rolesObj = resp.getBody().get("roles");
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (rolesObj instanceof List<?> rolesList) {
                    for (Object r : rolesList) {
                        authorities.add(new SimpleGrantedAuthority(r.toString()));
                    }
                } else if (rolesObj != null) {
                    authorities.add(new SimpleGrantedAuthority(rolesObj.toString()));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                chain.doFilter(request, response);
            }
            else {
                httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            }

        } catch (Exception e) {
            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }
    }
}
