package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthUserPort {
    Mono<UserAccount> findByUsername(String username);

    Mono<UserAccount> findById(UUID id);

    Mono<Void> unblockUser(UUID id);

    Mono<Void> incrementLoginAttempts(UUID id);

    Mono<Void> updateLastLogin(UUID id, LocalDateTime lastLogin);

    Flux<String> findActiveRoleNames(UUID userId);

    Mono<UserAccount> save(UserAccount user);
}

