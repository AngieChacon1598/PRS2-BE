package edu.pe.vallegrande.AuthenticationService.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/** Configuración de Swagger/OpenAPI para WebFlux */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication Service API")
                        .description("Microservicio de autenticación reactivo con Spring WebFlux y R2DBC\n\n" +
                                "**Rutas públicas (sin autenticación):**\n" +
                                "- POST /api/auth/login - Inicio de sesión\n" +
                                "- POST /api/auth/register - Registro de usuario\n" +
                                "- POST /api/auth/refresh - Renovar token\n" +
                                "- GET /actuator/health - Estado del servicio\n\n" +
                                "**Rutas protegidas:**\n" +
                                "Requieren Bearer Token en el header Authorization")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Valle Grande")
                                .email("support@vallegrande.edu.pe")))
                .servers(List.of(
                        new Server().url("http://localhost:5002").description("Servidor Local"),
                        new Server().url("https://your-production-url.com").description("Servidor Producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT en el formato: Bearer {token}")));
    }
}