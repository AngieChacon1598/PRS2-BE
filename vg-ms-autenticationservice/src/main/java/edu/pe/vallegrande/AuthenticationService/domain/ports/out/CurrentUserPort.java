package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface CurrentUserPort {
    Mono<UUID> currentUserId();

    Mono<UUID> currentMunicipalCode();
}

