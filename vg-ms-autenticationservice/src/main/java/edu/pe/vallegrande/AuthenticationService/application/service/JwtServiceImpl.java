package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Implementación del servicio JWT que valida contra Keycloak (Versión Reactiva)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final ReactiveJwtDecoder jwtDecoder;

    @Override
    public Mono<String> generateAccessToken(UUID userId, String username, UUID municipalCode, List<String> roles,
            List<String> permissions) {
        return Mono.error(new UnsupportedOperationException(
                "La generación local de tokens ha sido deshabilitada. Use Keycloak."));
    }

    @Override
    public Mono<String> generateRefreshToken(UUID userId, String username, UUID municipalCode) {
        return Mono.error(new UnsupportedOperationException(
                "La generación local de tokens ha sido deshabilitada. Use Keycloak."));
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> true)
                .onErrorResume(e -> {
                    log.error("Token de Keycloak inválido o expirado: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Claims> extractClaims(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    Claims claims = Jwts.claims()
                            .add(jwt.getClaims())
                            .subject(jwt.getSubject())
                            .expiration(jwt.getExpiresAt() != null ? Date.from(jwt.getExpiresAt()) : null)
                            .issuedAt(jwt.getIssuedAt() != null ? Date.from(jwt.getIssuedAt()) : null)
                            .build();
                    return claims;
                })
                .onErrorResume(e -> {
                    log.error("Error al extraer claims de token Keycloak: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<String> extractUsername(String token) {
        return extractClaims(token)
                .map(claims -> claims.getSubject());
    }

    @Override
    public Mono<UUID> extractUserId(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    String userIdStr = jwt.getClaimAsString("userId");
                    if (userIdStr == null) {
                        userIdStr = jwt.getSubject();
                    }
                    return UUID.fromString(userIdStr);
                })
                .onErrorResume(e -> {
                    log.error("Error al extraer userId: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<List<String>> extractRoles(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    List<String> roles = jwt.getClaimAsStringList("roles");
                    return roles != null ? roles : Collections.<String>emptyList();
                })
                .onErrorResume(e -> {
                    log.error("Error al extraer roles: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }

    @Override
    public Mono<List<String>> extractPermissions(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> {
                    List<String> permissions = jwt.getClaimAsStringList("permissions");
                    return permissions != null ? permissions : Collections.<String>emptyList();
                })
                .onErrorResume(e -> {
                    log.error("Error al extraer permisos: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }

    @Override
    public Mono<Boolean> isTokenExpired(String token) {
        return jwtDecoder.decode(token)
                .map(jwt -> jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now()))
                .onErrorReturn(true);
    }
}
