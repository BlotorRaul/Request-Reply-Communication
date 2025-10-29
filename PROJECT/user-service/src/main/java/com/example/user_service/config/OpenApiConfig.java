package com.example.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("API documentation for managing users and broadcasting user events via RabbitMQ")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartKYC / Energy Platform Dev Team")
                                .email("dev@platform.local")
                        )
                );
    }
}
