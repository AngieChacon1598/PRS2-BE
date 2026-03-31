package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.ports.out.TokenBlacklistPort;
import reactor.core.publisher.Mono;

@Component
public class InMemoryTokenBlacklistAdapter implements TokenBlacklistPort {

    private final Set<String> blacklisted = ConcurrentHashMap.newKeySet();

    @Override
    public Mono<Void> blacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklisted.add(token);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return Mono.just(false);
        }
        return Mono.just(blacklisted.contains(token));
    }
}

