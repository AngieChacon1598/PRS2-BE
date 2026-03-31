package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.exception.DuplicateAssignmentException;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.domain.model.PositionAllowedRole;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.PositionAllowedRoleRepository;
import pe.edu.vallegrande.configurationservice.infrastructure.config.JwtContextHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionAllowedRoleService {

    private final PositionAllowedRoleRepository repository;
    private final JwtContextHelper jwtContextHelper;

    public Flux<PositionAllowedRole> getAll() {
        return repository.findAll();
    }

    public Mono<PositionAllowedRole> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("PositionAllowedRole not found with id: " + id)));
    }

    public Flux<PositionAllowedRole> getByPositionId(UUID positionId) {
        return repository.findAllByPositionId(positionId);
    }

    public Flux<PositionAllowedRole> getByMunicipalityId(UUID municipalityId) {
        return repository.findAllByMunicipalityId(municipalityId);
    }

    public Flux<PositionAllowedRole> getByPositionAndMunicipality(UUID positionId, UUID municipalityId) {
        return repository.findAllByPositionIdAndMunicipalityId(positionId, municipalityId);
    }

    public Flux<PositionAllowedRole> getDefaultsByMunicipality(UUID municipalityId) {
        return repository.findAllByMunicipalityIdAndIsDefaultTrue(municipalityId);
    }

    public Mono<PositionAllowedRole> create(PositionAllowedRole entity) {
        return resolveUserId()
                .flatMap(userId -> {
                    entity.setCreatedBy(userId);
                    entity.setCreatedAt(LocalDateTime.now());
                    if (entity.getIsDefault() == null) entity.setIsDefault(false);
                    return repository
                            .findByPositionIdAndAreaIdAndRoleIdAndMunicipalityId(
                                    entity.getPositionId(), entity.getAreaId(),
                                    entity.getRoleId(), entity.getMunicipalityId())
                            .flatMap(existing -> Mono.<PositionAllowedRole>error(
                                    new DuplicateAssignmentException("Ya existe una asignación con ese cargo, área, rol y municipio")))
                            .switchIfEmpty(Mono.defer(() -> repository.save(entity)));
                });
    }

    public Mono<PositionAllowedRole> update(UUID id, PositionAllowedRole entity) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("PositionAllowedRole not found with id: " + id)))
                .flatMap(existing -> {
                    if (entity.getPositionId() != null) existing.setPositionId(entity.getPositionId());
                    if (entity.getAreaId() != null) existing.setAreaId(entity.getAreaId());
                    if (entity.getRoleId() != null) existing.setRoleId(entity.getRoleId());
                    if (entity.getIsDefault() != null) existing.setIsDefault(entity.getIsDefault());
                    if (entity.getMunicipalityId() != null) existing.setMunicipalityId(entity.getMunicipalityId());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("PositionAllowedRole not found with id: " + id)))
                .flatMap(repository::delete);
    }

    private Mono<UUID> resolveUserId() {
        return jwtContextHelper.getUserId();
    }
}
