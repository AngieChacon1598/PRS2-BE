package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.UserPermission;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AuthPermissionPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AuthUserPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.UserAccountMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.User;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.PermissionRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.RoleRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthPersistenceAdapter implements AuthUserPort, AuthPermissionPort {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Mono<UserAccount> findByUsername(String username) {
        return userRepository.findByUsername(username).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<UserAccount> findById(UUID id) {
        return userRepository.findById(id).map(UserAccountMapper::toDomain);
    }

    @Override
    public Mono<Void> unblockUser(UUID id) {
        return userRepository.unblockUser(id).then();
    }

    @Override
    public Mono<Void> incrementLoginAttempts(UUID id) {
        return userRepository.incrementLoginAttempts(id).then();
    }

    @Override
    public Mono<Void> updateLastLogin(UUID id, LocalDateTime lastLogin) {
        return userRepository.updateLastLogin(id, lastLogin).then();
    }

    @Override
    public Flux<String> findActiveRoleNames(UUID userId) {
        return userRoleRepository.findByUserIdAndActiveTrue(userId)
                .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()))
                .filter(role -> role.getActive() != null && role.getActive())
                .map(role -> role.getName());
    }

    @Override
    public Flux<UserPermission> findUserPermissions(UUID userId) {
        return permissionRepository.findUserPermissions(userId)
                .map(p -> UserPermission.builder()
                        .module(p.getModule())
                        .action(p.getAction())
                        .resource(p.getResource())
                        .build());
    }

    @Override
    public Mono<UserAccount> save(UserAccount user) {
        User entity = UserAccountMapper.toEntity(user);
        return userRepository.save(entity).map(UserAccountMapper::toDomain);
    }
}

