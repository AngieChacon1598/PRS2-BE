package edu.pe.vallegrande.AuthenticationService.infrastructure.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filtro de autenticación JWT - Deshabilitado porque entra en conflicto con
 * Keycloak OIDC
 */
@Slf4j
// @Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Permitir peticiones OPTIONS (preflight) sin validar JWT
        if (request.getMethod() != null && request.getMethod().name().equals("OPTIONS")) {
            log.debug("Permitiendo preflight request OPTIONS para: {}", path);
            return chain.filter(exchange);
        }

        // Permitir endpoints públicos sin autenticación
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extraer token del header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Request sin token JWT a endpoint protegido: {}", path);
            return chain.filter(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        return jwtService.validateToken(token)
                .flatMap(isValid -> {
                    if (isValid) {
                        return Mono.zip(
                                jwtService.extractUsername(token),
                                jwtService.extractRoles(token),
                                jwtService.extractPermissions(token),
                                jwtService.extractClaims(token)
                        ).flatMap(tuple -> {
                            String username = tuple.getT1();
                            List<String> roles = tuple.getT2();
                            List<String> permissions = tuple.getT3();
                            io.jsonwebtoken.Claims claims = tuple.getT4();

                            log.debug("Usuario autenticado: {} con roles: {} y permisos: {}", username, roles, permissions);

                            List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());

                            if (permissions != null) {
                                authorities.addAll(permissions.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList()));
                            }

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    claims,
                                    null,
                                    authorities);

                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        });
                    } else {
                        log.warn("Token JWT inválido para path: {}", path);
                        return chain.filter(exchange);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error al procesar token JWT: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

    /** Verifica si el path es público */
    private boolean isPublicPath(String path) {
        return path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/refresh") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.equals("/actuator/health");
    }
}
