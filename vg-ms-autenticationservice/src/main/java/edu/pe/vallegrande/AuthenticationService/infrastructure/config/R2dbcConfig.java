package edu.pe.vallegrande.AuthenticationService.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/** Configuración de R2DBC */
@Configuration
@EnableR2dbcRepositories(basePackages = "edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository")
public class R2dbcConfig {

    /** Manejador de transacciones reactivo */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}