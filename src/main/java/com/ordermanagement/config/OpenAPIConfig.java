package com.ordermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
public class OpenAPIConfig {

        @Bean
        public OpenAPI orderManagementOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Order Management API")
                                                .description(
                                                                "Backend service for managing order lifecycle with async processing and idempotency")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("API Support")
                                                                .email("support@ordermanagement.com")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Local Development"),
                                                new Server().url("http://136.113.173.5:8080")
                                                                .description("Production Server (GCP)")));
        }
}
