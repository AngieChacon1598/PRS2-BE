package edu.pe.vallegrande.AuthenticationService.infrastructure.config;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/** Configuración de zona horaria (America/Lima) */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer zona horaria por defecto de la JVM a America/Lima (Perú)
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        System.setProperty("user.timezone", "America/Lima");
        
        System.out.println("Zona horaria configurada: " + ZoneId.systemDefault());
        System.out.println("TimeZone: " + TimeZone.getDefault().getID());
    }
}
