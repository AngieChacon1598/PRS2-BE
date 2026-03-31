package pe.edu.vallegrande.ms_inventory.infrastructure.config;

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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Mapeo de roles a permisos de inventario
    private static final Map<String, List<String>> ROLE_PERMISSIONS = new HashMap<>();
    static {
        List<String> allPerms = Arrays.asList(
                "inventario:read", "inventario:create", "inventario:update",
                "inventario:close", "inventario:verify");
        ROLE_PERMISSIONS.put("INVENTARIO_COORDINADOR", allPerms);
        ROLE_PERMISSIONS.put("INVENTARIO_VERIFICADOR", Arrays.asList("inventario:read", "inventario:verify"));
        ROLE_PERMISSIONS.put("SUPER_ADMIN", allPerms);
        ROLE_PERMISSIONS.put("TENANT_ADMIN", allPerms);
        ROLE_PERMISSIONS.put("PATRIMONIO_GESTOR", Arrays.asList("inventario:read"));
    }

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

            // Mapear roles a permisos automáticamente
            roles.forEach(role -> {
                List<String> perms = ROLE_PERMISSIONS.get(role.toUpperCase());
                if (perms != null) {
                    perms.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                    log.debug("Permisos asignados por rol {}: {}", role, perms);
                }
            });
        }

        // Si ya tiene permisos por rol, retornar sin ir a Redis
        boolean hasInventoryPerms = authorities.stream()
                .anyMatch(a -> a.getAuthority().startsWith("inventario:"));
        if (hasInventoryPerms) {
            log.debug("Permisos asignados por rol, total authorities: {}", authorities.size());
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        }

        // Fallback: leer permisos del claim permissions del JWT
        List<String> jwtPermissions = jwt.getClaimAsStringList("permissions");
        if (jwtPermissions != null && !jwtPermissions.isEmpty()) {
            authorities.addAll(jwtPermissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        }

        // Fallback: leer permisos desde Redis
        String userId = jwt.getClaimAsString("user_id");
        if (userId == null)
            userId = jwt.getClaimAsString("userId");
        String municipalCode = jwt.getClaimAsString("municipal_code");
        if (municipalCode == null)
            municipalCode = jwt.getClaimAsString("municipalCode");

        if (userId == null) {
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        }

        String redisKey = "perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform");

        return redisTemplate.opsForValue()
                .get(redisKey)
                .map(this::parseJson)
                .defaultIfEmpty(Collections.emptyList())
                .map(permissions -> {
                    authorities.addAll(permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));
                    return (AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities);
                })
                .doOnError(e -> log.error("Error Redis: {}", e.getMessage()))
                .onErrorReturn(new JwtAuthenticationToken(jwt, authorities));
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
