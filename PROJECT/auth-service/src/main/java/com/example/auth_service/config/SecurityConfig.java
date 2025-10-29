package com.example.auth_service.config;

import com.example.auth_service.security.CustomUserDetailsService;
import com.example.auth_service.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("Auth mode: " + authMode.toUpperCase());
        http.csrf(AbstractHttpConfigurer::disable);

        switch (authMode.toLowerCase()) {
            case "basic" -> {
                System.out.println("BASIC Authentication ENABLED (DB-based)");
                http
                        .authorizeHttpRequests(auth -> auth
                                //Permitem accesul la Swagger fara autentificare
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                                // Login endpoint e public
                                .requestMatchers("/api/auth/users/login").permitAll()

                                // restul trebuie autentificat
                                .anyRequest().authenticated()
                        )
                        .userDetailsService(customUserDetailsService)
                        .httpBasic(basic -> {});
            }

            case "jwt" -> {
                System.out.println("JWT Authentication ENABLED");
                http
                        .authorizeHttpRequests(auth -> auth
                                //Permitem accesul la Swagger fara autentificare
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                                // Login este public
                                .requestMatchers("/api/auth/users/login").permitAll()

                                //  doar USER are acces la acest endpoint
                                .requestMatchers("/api/auth/users/test-user").hasRole("USER")

                                // doar ADMIN are acces la celelalte endpointuri
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
