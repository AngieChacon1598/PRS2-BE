package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PermissionPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.PermissionMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PermissionPersistenceAdapter implements PermissionPort {
    private final PermissionRepository permissionRepository;

    @Override
    public Mono<Boolean> existsByModuleAndActionAndResource(String module, String action, String resource) {
        return permissionRepository.existsByModuleAndActionAndResource(module, action, resource);
    }

    @Override
    public Mono<PermissionModel> save(PermissionModel permission) {
        return permissionRepository.save(PermissionMapper.toEntity(permission)).map(PermissionMapper::toDomain);
    }

    @Override
    public Mono<PermissionModel> findById(UUID id) {
        return permissionRepository.findById(id).map(PermissionMapper::toDomain);
    }

    @Override
    public Mono<PermissionModel> findByModuleAndActionAndResource(String module, String action, String resource) {
        return permissionRepository.findByModuleAndActionAndResource(module, action, resource)
                .map(PermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findAll() {
        return permissionRepository.findAll().map(PermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findAllByMunicipalCode(UUID municipalCode) {
        return permissionRepository.findByMunicipalCode(municipalCode).map(PermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findByModule(String module) {
        return permissionRepository.findByModule(module).map(PermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findByStatus(Boolean status) {
        return permissionRepository.findByStatus(status).map(PermissionMapper::toDomain);
    }

    @Override
    public Flux<PermissionModel> findByModuleAndStatus(String module, Boolean status) {
        return permissionRepository.findByModuleAndStatus(module, status).map(PermissionMapper::toDomain);
    }
}

