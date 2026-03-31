package edu.pe.vallegrande.AuthenticationService.infrastructure.security;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/** Utilidades de seguridad y contexto de usuario autenticado */
@Slf4j
public class SecurityUtils {

    /** @deprecated Usar RoleConstants.SUPER_ADMIN */
    @Deprecated
    public static final String ROLE_SUPER_ADMIN = RoleConstants.SUPER_ADMIN;

    /** @deprecated Usar RoleConstants.ADMIN */
    @Deprecated
    public static final String ROLE_ADMIN = RoleConstants.ADMIN;

    /** @deprecated Usar RoleConstants.USER_MANAGER */
    @Deprecated
    public static final String ROLE_USER_MANAGER = RoleConstants.USER_MANAGER;

    /** @deprecated Usar RoleConstants.VIEWER */
    @Deprecated
    public static final String ROLE_VIEWER = RoleConstants.VIEWER;

    /** Obtiene el ID del usuario autenticado */
    public static Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    Object principal = auth.getPrincipal();
                    Object userIdObj = null;

                    if (principal instanceof Jwt) {
                        Jwt jwt = (Jwt) principal;
                        // Intentar obtener "user_id" (claim personalizado) o "sub" (estándar de Keycloak)
                        userIdObj = jwt.getClaim("user_id");
                        if (userIdObj == null) {
                            userIdObj = jwt.getSubject();
                        }
                    } else if (principal instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> claims = (Map<String, Object>) principal;
                        userIdObj = claims.get("userId");
                    }

                    if (userIdObj != null) {
                        try {
                            return Mono.just(UUID.fromString(userIdObj.toString()));
                        } catch (IllegalArgumentException e) {
                            log.error("Error al parsear userId: {}", userIdObj, e);
                            return Mono.empty();
                        }
                    }

                    log.warn("No se pudo extraer userId del contexto de seguridad. Principal type: {}", principal.getClass().getName());
                    return Mono.empty();
                });
    }

    /** Obtiene el código municipal del usuario autenticado */
    public static Mono<UUID> getCurrentUserMunicipalCode() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    Object principal = auth.getPrincipal();
                    Object municipalCodeObj = null;

                    if (principal instanceof Jwt) {
                        Jwt jwt = (Jwt) principal;
                        municipalCodeObj = jwt.getClaim("municipal_code");
                    } else if (principal instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> claims = (Map<String, Object>) principal;
                        municipalCodeObj = claims.get("municipalCode");
                    }

                    if (municipalCodeObj != null) {
                        try {
                            return Mono.just(UUID.fromString(municipalCodeObj.toString()));
                        } catch (IllegalArgumentException e) {
                            log.error("Error al parsear municipalCode: {}", municipalCodeObj, e);
                            return Mono.empty();
                        }
                    }

                    log.warn("No se pudo extraer municipalCode del contexto de seguridad. Principal type: {}", principal.getClass().getName());
                    return Mono.empty();
                });
    }

    /** Obtiene el username del usuario autenticado */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .switchIfEmpty(Mono.empty());
    }

    /** Verifica si el usuario tiene un rol específico */
    public static Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role)))
                .defaultIfEmpty(false);
    }

    /** Verifica si el usuario tiene alguno de los roles especificados */
    public static Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    for (String role : roles) {
                        if (auth.getAuthorities().stream()
                                .anyMatch(ga -> ga.getAuthority().equals("ROLE_" + role))) {
                            return true;
                        }
                    }
                    return false;
                })
                .defaultIfEmpty(false);
    }

}
