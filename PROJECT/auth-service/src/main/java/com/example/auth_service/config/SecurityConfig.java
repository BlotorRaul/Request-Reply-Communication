package com.example.auth_service.config;

import com.example.auth_service.security.CustomUserDetailsService;
import com.example.auth_service.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

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
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .authorizeHttpRequests(auth -> auth
                                // Endpoints publice - trebuie să fie primele
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/api/auth/users/login",
                                        "/api/auth/users/validate-jwt",
                                        "/api/auth/users/mode"
                                ).permitAll()
                                // POST pentru creare utilizatori - permite ADMIN sau fără autentificare (pentru inițializare)
                                .requestMatchers(HttpMethod.POST, "/api/auth/users").permitAll()
                                // Endpoints pentru USER - doar GET
                                .requestMatchers("/api/auth/users/test-user").hasRole("USER")
                                // Endpoints pentru ADMIN
                                .requestMatchers(HttpMethod.GET, "/api/auth/users").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/auth/users/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/users/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/auth/users/{id}").hasRole("ADMIN")
                                // Orice alt request necesita autentificare
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Allow all origins in Docker
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
