package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.security;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${spring.data.redis.ttl}")
    private Duration ttl;

    @Override
    public Mono<List<String>> getPermissions(String userId, String municipalCode) {
        return redisTemplate.opsForValue()
                .get("perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform"))
                .map(this::parseJson)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Void> setPermissions(String userId, String municipalCode, List<String> permissions) {
        return redisTemplate.opsForValue()
                .set("perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform"),
                        toJson(permissions), ttl)
                .then();
    }

    @Override
    public Mono<Void> invalidate(String userId, String municipalCode) {
        return redisTemplate.delete("perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform"))
                .then();
    }

    private List<String> parseJson(String json) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("Error al parsear permisos desde Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String toJson(List<String> permissions) {
        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar permisos para Redis: {}", e.getMessage());
            return "[]";
        }
    }
}
