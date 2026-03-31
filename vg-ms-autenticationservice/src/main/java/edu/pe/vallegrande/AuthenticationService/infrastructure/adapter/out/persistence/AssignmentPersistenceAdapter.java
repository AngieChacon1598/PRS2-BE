package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionLink;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleLink;
import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AssignmentPermissionQueryPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePermissionPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserRolePort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.PermissionMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.RolePermissionMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.UserRoleMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.PermissionRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.RolePermissionRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssignmentPersistenceAdapter implements UserRolePort, RolePermissionPort, AssignmentPermissionQueryPort {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Flux<UserRoleLink> findByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId).map(UserRoleMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUserIdAndRoleId(UUID userId, UUID roleId) {
        return userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public Mono<Void> deleteByUserIdAndRoleId(UUID userId, UUID roleId) {
        return userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public Mono<UserRoleLink> save(UserRoleLink link) {
        return userRoleRepository.save(UserRoleMapper.toEntity(link)).map(UserRoleMapper::toDomain);
    }

    @Override
    public Flux<RolePermissionLink> findByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleId(roleId).map(RolePermissionMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public Mono<Void> deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public Mono<RolePermissionLink> save(RolePermissionLink link) {
        return rolePermissionRepository.save(RolePermissionMapper.toEntity(link)).map(RolePermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findUserPermissions(UUID userId) {
        return permissionRepository.findUserPermissions(userId).map(PermissionMapper::toDomain);
    }
}

