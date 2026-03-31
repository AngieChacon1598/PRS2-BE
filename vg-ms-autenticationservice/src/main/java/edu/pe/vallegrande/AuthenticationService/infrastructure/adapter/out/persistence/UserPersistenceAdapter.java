package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.UserAccountMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {
    private final UserRepository userRepository;

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByUsernameAndIdNot(String username, UUID id) {
        return userRepository.existsByUsernameAndIdNot(username, id);
    }

    @Override
    public Flux<UserAccount> findAll() {
        return userRepository.findAll().map(UserAccountMapper::toDomain);
    }

    @Override
    public Flux<UserAccount> findAllByMunicipalCode(UUID municipalCode) {
        return userRepository.findByMunicipalCode(municipalCode).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<UserAccount> findById(UUID id) {
        return userRepository.findById(id).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<UserAccount> findByUsername(String username) {
        return userRepository.findByUsername(username).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<UserAccount> save(UserAccount user) {
        return userRepository.save(UserAccountMapper.toEntity(user)).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, String status, UUID updatedBy) {
        return userRepository.updateStatus(id, status, updatedBy).then();
    }

    @Override
    public Mono<Void> updateLastLogin(UUID id, LocalDateTime lastLogin) {
        return userRepository.updateLastLogin(id, lastLogin).then();
    }

    @Override
    public Mono<Void> incrementLoginAttempts(UUID id) {
        return userRepository.incrementLoginAttempts(id).then();
    }
}

