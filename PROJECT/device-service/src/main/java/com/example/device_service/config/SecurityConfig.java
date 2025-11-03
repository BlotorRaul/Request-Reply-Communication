package com.example.device_service.config;

import com.example.device_service.security.RemoteAuthModeProvider;
import com.example.device_service.security.RemoteBasicAuthFilter;
import com.example.device_service.security.RemoteJwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class SecurityConfig {

    private final RemoteAuthModeProvider modeProvider;
    private final RemoteBasicAuthFilter basicFilter;
    private final RemoteJwtAuthFilter jwtFilter;

    public SecurityConfig(RemoteAuthModeProvider modeProvider,
                          RemoteBasicAuthFilter basicFilter,
                          RemoteJwtAuthFilter jwtFilter) {
        this.modeProvider = modeProvider;
        this.basicFilter = basicFilter;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String mode = modeProvider.getAuthMode();
        System.out.println("🔐 Auth mode detected: " + mode.toUpperCase());

        http.csrf(csrf -> csrf.disable());

        // Dacă modul e DEFAULT, permitem totul și nu adăugăm filtre
        if (mode.equalsIgnoreCase("default")) {
            System.out.println("⚠️ Authentication disabled (default mode)");
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );
            return http.build();
        }

        // Configurăm regulile pentru BASIC și JWT
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        // Alegem filtrul potrivit în funcție de modul
        if (mode.equalsIgnoreCase("jwt")) {
            System.out.println("✅ Using JWT authentication");
            http.addFilterBefore(jwtFilter, BasicAuthenticationFilter.class);
        } else if (mode.equalsIgnoreCase("basic")) {
            System.out.println("✅ Using Basic authentication");
            http.addFilterBefore(basicFilter, BasicAuthenticationFilter.class);
        }

        return http.build();
    }

    // Disable automatic registration so filters only run when explicitly added based on mode
    @Bean
    public FilterRegistrationBean<RemoteJwtAuthFilter> disableJwtAutoRegistration(RemoteJwtAuthFilter filter) {
        FilterRegistrationBean<RemoteJwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RemoteBasicAuthFilter> disableBasicAutoRegistration(RemoteBasicAuthFilter filter) {
        FilterRegistrationBean<RemoteBasicAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
