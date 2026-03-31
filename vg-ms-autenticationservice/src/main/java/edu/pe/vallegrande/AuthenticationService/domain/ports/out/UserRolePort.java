package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleLink;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRolePort {
    Flux<UserRoleLink> findByUserId(UUID userId);

    Mono<Boolean> existsByUserIdAndRoleId(UUID userId, UUID roleId);

    Mono<Void> deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    Mono<UserRoleLink> save(UserRoleLink link);
}

