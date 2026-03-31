package pe.edu.vallegrande.ms_maintenanceService.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationConverter(ReactiveRedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = extractRoles(jwt);
        if (roles != null && !roles.isEmpty()) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));
            log.debug("Roles extraídos: {}", roles);
        }

        List<String> jwtPermissions = jwt.getClaimAsStringList("permissions");
        if (jwtPermissions != null && !jwtPermissions.isEmpty()) {
            authorities.addAll(jwtPermissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
            log.debug("Permisos del JWT: {} permisos", jwtPermissions.size());
            return Mono.just((AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities));
        }

        String userId = jwt.getClaimAsString("user_id");
        if (userId == null) {
            userId = jwt.getClaimAsString("userId");
        }

        String municipalCode = jwt.getClaimAsString("municipal_code");
        if (municipalCode == null) {
            municipalCode = jwt.getClaimAsString("municipalCode");
        }

        if (userId == null) {
            log.debug("JWT sin userId, retornando solo roles: {}", roles);
            return Mono.just((AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities));
        }

        String redisKey = "perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform");
        log.debug("Buscando permisos en Redis: {}", redisKey);

        return redisTemplate.opsForValue()
                .get(redisKey)
                .map(this::parseJson)
                .defaultIfEmpty(Collections.emptyList())
                .map(permissions -> {
                    authorities.addAll(permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));
                    log.debug("Permisos Redis: {} (total authorities: {})", permissions.size(), authorities.size());
                    return (AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities);
                })
                .doOnError(e -> log.error("Error Redis: {}", e.getMessage()))
                .onErrorReturn((AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }
        }

        return jwt.getClaimAsStringList("roles");
    }

    private List<String> parseJson(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("Error al parsear permisos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

