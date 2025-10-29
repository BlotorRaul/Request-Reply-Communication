package com.example.auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("API documentation for Authentication microservice using JWT")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Energy Platform Dev Team")
                                .email("dev@energy-platform.local")
                        )
                );
    }
}
