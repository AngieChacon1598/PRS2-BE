package edu.pe.vallegrande.AuthenticationService.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/** Configuración adicional de WebFlux */
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {
}
