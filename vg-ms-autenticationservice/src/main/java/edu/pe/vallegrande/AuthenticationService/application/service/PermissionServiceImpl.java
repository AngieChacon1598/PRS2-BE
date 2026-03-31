package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.pe.vallegrande.AuthenticationService.domain.exception.DuplicateResourceException;
import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PermissionService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PermissionPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CachePort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AuthPermissionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionPort permissionPort;
    private final CachePort cachePort;
    private final AuthPermissionPort authPermissionPort;

    @Override
    public Mono<PermissionModel> createPermission(PermissionModel permission) {
        PermissionModel toSave = permission.toBuilder()
                .createdAt(LocalDateTime.now())
                .status(true)
                .build();

        // Check if permission with same module, action, and resource already exists
        return permissionPort.existsByModuleAndActionAndResource(
                toSave.getModule(), toSave.getAction(), toSave.getResource())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono
                                .error(new DuplicateResourceException("Permission already exists with these details"));
                    }
                    return permissionPort.save(toSave);
                });
    }

    @Override
    public Mono<PermissionModel> getPermissionById(UUID id) {
        return permissionPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Permission not found with id: " + id)));
    }

    @Override
    public Mono<PermissionModel> getPermissionByDetails(String module, String action, String resource) {
        return permissionPort.findByModuleAndActionAndResource(module, action, resource)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Permission not found with module: " + module + ", action: " + action + ", resource: "
                                + resource)));
    }

    @Override
    public Flux<PermissionModel> getAllPermissions(String module, Boolean status, UUID municipalCode) {
        log.info("Obteniendo permisos con filtros - module: {}, status: {}, municipalCode: {}", module, status, municipalCode);
        
        Flux<PermissionModel> permissions = (municipalCode != null)
                ? permissionPort.findAllByMunicipalCode(municipalCode)
                : permissionPort.findAll();

        if (module != null) {
            permissions = permissions.filter(p -> module.equals(p.getModule()));
        }
        if (status != null) {
            permissions = permissions.filter(p -> status.equals(p.getStatus()));
        }
        
        return permissions;
    }

    @Override
    public Mono<PermissionModel> updatePermission(UUID id, PermissionModel permission) {
        return permissionPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Permission not found with id: " + id)))
                .flatMap(existing -> {
                    PermissionModel updated = existing.toBuilder()
                            .module(permission.getModule())
                            .action(permission.getAction())
                            .resource(permission.getResource())
                            .displayName(permission.getDisplayName())
                            .description(permission.getDescription())
                            .municipalCode(permission.getMunicipalCode() != null ? permission.getMunicipalCode()
                                    : existing.getMunicipalCode())
                            .build();
                    return permissionPort.save(updated);
                });
    }

    @Override
    public Mono<Void> deletePermission(UUID id) {
        return permissionPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Permission not found with id: " + id)))
                .flatMap(permission -> {
                    return permissionPort.save(permission.toBuilder().status(false).build());
                })
                .then();
    }

    @Override
    public Mono<PermissionModel> restorePermission(UUID id) {
        return permissionPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Permission not found with id: " + id)))
                .flatMap(permission -> {
                    return permissionPort.save(permission.toBuilder().status(true).build());
                });
    }

    @Override
    public Flux<String> getUserPermissions(UUID userId, UUID municipalCode) {
        String uId = userId.toString();
        String mCode = municipalCode != null ? municipalCode.toString() : null;

        return cachePort.getPermissions(uId, mCode)
                .flatMapMany(Flux::fromIterable)
                .switchIfEmpty(
                    authPermissionPort.findUserPermissions(userId)
                        .map(p -> p.getModule() + ":" + p.getAction() + (p.getResource() != null ? ":" + p.getResource() : ""))
                        .collectList()
                        .flatMapMany(perms -> 
                            cachePort.setPermissions(uId, mCode, perms)
                                .thenMany(Flux.fromIterable(perms))
                        )
                );
    }
}
