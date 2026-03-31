package pe.edu.vallegrande.configurationservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class DatabaseConfig {
    // La configuración se toma automáticamente de application.yml
    // El problema de SQL se solucionó con el mapeo explícito de columnas en el modelo
}