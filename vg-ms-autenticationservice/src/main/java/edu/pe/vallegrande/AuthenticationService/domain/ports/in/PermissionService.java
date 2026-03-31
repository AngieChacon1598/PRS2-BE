package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PermissionService {
    Mono<PermissionModel> createPermission(PermissionModel permission);
    Mono<PermissionModel> getPermissionById(UUID id);
    Mono<PermissionModel> getPermissionByDetails(String module, String action, String resource);
    Flux<PermissionModel> getAllPermissions(String module, Boolean status, UUID municipalCode);
    Mono<PermissionModel> updatePermission(UUID id, PermissionModel permission);
    Mono<Void> deletePermission(UUID id);
    Mono<PermissionModel> restorePermission(UUID id);
    Flux<String> getUserPermissions(UUID userId, UUID municipalCode);
}