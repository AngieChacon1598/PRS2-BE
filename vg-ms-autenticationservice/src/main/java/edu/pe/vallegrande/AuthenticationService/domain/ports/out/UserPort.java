package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserPort {
    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByUsernameAndIdNot(String username, UUID id);

    Flux<UserAccount> findAll();

    Flux<UserAccount> findAllByMunicipalCode(UUID municipalCode);

    Mono<UserAccount> findById(UUID id);

    Mono<UserAccount> findByUsername(String username);

    Mono<UserAccount> save(UserAccount user);

    Mono<Void> updateStatus(UUID id, String status, UUID updatedBy);

    Mono<Void> updateLastLogin(UUID id, LocalDateTime lastLogin);

    Mono<Void> incrementLoginAttempts(UUID id);
}

