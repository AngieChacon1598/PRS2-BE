package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.RoleMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolePort {
    private final RoleRepository roleRepository;

    @Override
    public Mono<Boolean> existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    public Mono<Boolean> existsByNameAndIdNot(String name, UUID id) {
        return roleRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Mono<RoleModel> save(RoleModel role) {
        return roleRepository.save(RoleMapper.toEntity(role)).map(RoleMapper::toDomain);
    }

    @Override
    public Flux<RoleModel> findAll() {
        return roleRepository.findAll().map(RoleMapper::toDomain);
    }

    @Override
    public Flux<RoleModel> findByActiveTrue() {
        return roleRepository.findByActiveTrue().map(RoleMapper::toDomain);
    }

    @Override
    public Flux<RoleModel> findByActiveFalse() {
        return roleRepository.findByActiveFalse().map(RoleMapper::toDomain);
    }

    @Override
    public Mono<RoleModel> findById(UUID id) {
        return roleRepository.findById(id).map(RoleMapper::toDomain);
    }

    @Override
    public Mono<RoleModel> findByName(String name) {
        return roleRepository.findByName(name).map(RoleMapper::toDomain);
    }

    @Override
    public Mono<Void> updateActiveStatus(UUID id, boolean active) {
        return roleRepository.updateActiveStatus(id, active).then();
    }
}

