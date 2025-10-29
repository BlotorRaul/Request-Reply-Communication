package com.example.device_service.config;


import com.example.device_service.security.RemoteBasicAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final RemoteBasicAuthFilter remoteBasicAuthFilter;

    public SecurityConfig(RemoteBasicAuthFilter remoteBasicAuthFilter) {
        this.remoteBasicAuthFilter = remoteBasicAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(remoteBasicAuthFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}