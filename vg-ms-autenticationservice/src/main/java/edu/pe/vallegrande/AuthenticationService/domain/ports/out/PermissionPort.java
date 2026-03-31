package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PermissionPort {
    Mono<Boolean> existsByModuleAndActionAndResource(String module, String action, String resource);

    Mono<PermissionModel> save(PermissionModel permission);

    Mono<PermissionModel> findById(UUID id);

    Mono<PermissionModel> findByModuleAndActionAndResource(String module, String action, String resource);

    Flux<PermissionModel> findAll();

    Flux<PermissionModel> findAllByMunicipalCode(UUID municipalCode);

    Flux<PermissionModel> findByModule(String module);

    Flux<PermissionModel> findByStatus(Boolean status);

    Flux<PermissionModel> findByModuleAndStatus(String module, Boolean status);
}

