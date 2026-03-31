package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import reactor.core.publisher.Mono;

public interface TokenBlacklistPort {
    Mono<Void> blacklist(String token);

    Mono<Boolean> isBlacklisted(String token);
}

