package com.clicktucasa.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI document metadata (Pilar 3 of the Hito 4 rubric). SpringDoc uses
 * this bean, together with the {@code @Tag}/{@code @Operation}/
 * {@code @Schema} annotations on the controllers and DTOs, to generate
 * the live contract served at {@code /api-docs} and rendered by
 * Swagger UI at {@code /swagger-ui.html} — both gated to the {@code dev}
 * profile in {@code application.yml}/{@code application-dev.yml}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clickTuCasaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClickTuCasa API")
                        .description("House raffle ticketing microservice — Clean Architecture / DDD, Spring Boot, PostgreSQL and Docker")
                        .version("v1.0 (Hito 4)")
                        .contact(new Contact().name("Daniel Araya Rocha"))
                        .license(new License().name("Educational use - Desafio Latam / Globant Talento Ready")));
    }
}
