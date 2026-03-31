package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionLink;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePermissionPort {
    Flux<RolePermissionLink> findByRoleId(UUID roleId);

    Mono<Boolean> existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    Mono<Void> deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    Mono<RolePermissionLink> save(RolePermissionLink link);
}

