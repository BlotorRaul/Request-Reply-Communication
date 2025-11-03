package com.example.auth_service.config;

import com.example.auth_service.security.CustomUserDetailsService;
import com.example.auth_service.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${auth.mode:default}")
    private String authMode;

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Bean necesar pentru AuthValidationController
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("Auth mode: " + authMode.toUpperCase());
        http.csrf(AbstractHttpConfigurer::disable);

        switch (authMode.toLowerCase()) {
            case "basic" -> {
                System.out.println("BASIC Authentication ENABLED (DB-based)");
                http
                        .authorizeHttpRequests(auth -> auth
                                // Permitem accesul la Swagger si la login/validate fără autentificare
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/api/auth/users/login",
                                        "/api/auth/validate-basic",
                                        "/api/auth/users/mode"
                                ).permitAll()

                                // Orice alt request necesita autentificare
                                .anyRequest().authenticated()
                        )
                        .userDetailsService(customUserDetailsService)
                        .httpBasic(basic -> {});
            }

            case "jwt" -> {
                System.out.println("JWT Authentication ENABLED");
                http
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/api/auth/users/login",
                                        "/api/auth/users/validate-jwt",
                                        "/api/auth/users/mode"
                                ).permitAll()
                                .requestMatchers("/api/auth/users/test-user").hasRole("USER")
                                .requestMatchers("/api/auth/users/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                        )
                        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .userDetailsService(customUserDetailsService)
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            }

            default -> {
                System.out.println("Authentication DISABLED (permitAll)");
                http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            }
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
