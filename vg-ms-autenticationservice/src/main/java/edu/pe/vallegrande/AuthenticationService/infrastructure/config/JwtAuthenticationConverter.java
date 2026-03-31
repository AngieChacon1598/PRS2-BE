package edu.pe.vallegrande.AuthenticationService.infrastructure.config;

import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PermissionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Convierte un token JWT de Keycloak en un objeto Authentication reconocible por Spring Security,
 * extrayendo tanto los roles del claim "roles", como los permisos granulares desde Redis/DB.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final PermissionService permissionService;

    public JwtAuthenticationConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        // 1. Extraer Roles del JWT
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));
        }

        // 2. Extraer user_id y municipal_code
        String userIdStr = jwt.getClaimAsString("user_id");
        String municipalCodeStr = jwt.getClaimAsString("municipal_code");
        
        if (userIdStr == null) {
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        }
        
        UUID municipalCode = null;
        if (municipalCodeStr != null && !municipalCodeStr.isBlank()) {
            try {
                municipalCode = UUID.fromString(municipalCodeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        // 3. Obtener permisos finos (Capa 3) del cache Redis
        return permissionService.getUserPermissions(userId, municipalCode)
                .map(SimpleGrantedAuthority::new)
                .collectList()
                .map(permissions -> {
                    authorities.addAll(permissions);
                    return new JwtAuthenticationToken(jwt, authorities);
                });
    }
}
