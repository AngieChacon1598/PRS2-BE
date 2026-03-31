package pe.edu.vallegrande.configurationservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${redis.enabled:false}")
    private boolean redisEnabled;

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        String userId = jwt.getSubject();
        String municipalCode = jwt.getClaimAsString("municipal_code");

        List<GrantedAuthority> roles = extractRoles(jwt);

        if (!redisEnabled || userId == null || municipalCode == null) {
            if (!redisEnabled) log.debug("Redis deshabilitado, usando solo roles del JWT");
            return Mono.just(new JwtAuthenticationToken(jwt, roles));
        }

        String redisKey = "perms:" + userId + ":" + municipalCode;

        return redisTemplate.opsForSet().members(redisKey)
                .collectList()
                .timeout(Duration.ofSeconds(3))
                .map(perms -> {
                    List<GrantedAuthority> authorities = new ArrayList<>(roles);
                    perms.stream()
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                    log.debug("Authorities para {}: roles={}, perms={}", userId, roles.size(), perms.size());
                    return (AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities);
                })
                .onErrorResume(ex -> {
                    log.error("Error consultando permisos en Redis para key {}: {}", redisKey, ex.getMessage());
                    return Mono.just(new JwtAuthenticationToken(jwt, roles));
                });
    }
//cambio
    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptyList();

        List<String> roles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}

