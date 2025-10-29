package com.example.device_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Device Service API")
                        .description("API documentation for managing devices and local user synchronization")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Energy Platform Dev Team")
                                .email("dev@energy-platform.local")
                        )
                );
    }
}
