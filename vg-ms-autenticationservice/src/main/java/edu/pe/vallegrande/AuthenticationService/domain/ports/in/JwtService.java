package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

import io.jsonwebtoken.Claims;

/**
 * Servicio para manejo de tokens JWT (Versión Reactiva)
 */
public interface JwtService {
    
    /**
     * Generar token de acceso
     */
    Mono<String> generateAccessToken(UUID userId, String username, UUID municipalCode, List<String> roles, List<String> permissions);
    
    /**
     * Generar token de refresh
     */
    Mono<String> generateRefreshToken(UUID userId, String username, UUID municipalCode);
    
    /**
     * Validar token
     */
    Mono<Boolean> validateToken(String token);
    
    /**
     * Extraer claims del token
     */
    Mono<Claims> extractClaims(String token);
    
    /**
     * Extraer username del token
     */
    Mono<String> extractUsername(String token);
    
    /**
     * Extraer user ID del token
     */
    Mono<UUID> extractUserId(String token);
    
    /**
     * Extraer roles del token
     */
    Mono<List<String>> extractRoles(String token);

    /**
     * Extraer permisos del token
     */
    Mono<List<String>> extractPermissions(String token);
    
    /**
     * Verificar si el token ha expirado
     */
    Mono<Boolean> isTokenExpired(String token);
}