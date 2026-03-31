package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePort {
    Mono<Boolean> existsByName(String name);

    Mono<Boolean> existsByNameAndIdNot(String name, UUID id);

    Mono<RoleModel> save(RoleModel role);

    Flux<RoleModel> findAll();

    Flux<RoleModel> findByActiveTrue();

    Flux<RoleModel> findByActiveFalse();

    Mono<RoleModel> findById(UUID id);

    Mono<RoleModel> findByName(String name);

    Mono<Void> updateActiveStatus(UUID id, boolean active);
}

