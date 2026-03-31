package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.Area;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.AreaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository repository;

    public Flux<Area> getAll() {
        return repository.findAll();
    }

    public Flux<Area> getAllActive() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    public Flux<Area> getAllInactive() {
        return repository.findAllByActiveFalseOrderByNameAsc();
    }

    public Mono<Area> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Area not found with id: " + id)));
    }

    public Mono<Area> create(Area entity) {
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public Mono<Area> update(UUID id, Area data) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (data.getAreaCode() != null) existing.setAreaCode(data.getAreaCode());
                    if (data.getName() != null) existing.setName(data.getName());
                    if (data.getDescription() != null) existing.setDescription(data.getDescription());
                    if (data.getParentAreaId() != null) existing.setParentAreaId(data.getParentAreaId());
                    if (data.getHierarchicalLevel() != null) existing.setHierarchicalLevel(data.getHierarchicalLevel());
                    if (data.getResponsibleId() != null) existing.setResponsibleId(data.getResponsibleId());
                    if (data.getPhysicalLocation() != null) existing.setPhysicalLocation(data.getPhysicalLocation());
                    if (data.getPhone() != null) existing.setPhone(data.getPhone());
                    if (data.getEmail() != null) existing.setEmail(data.getEmail());
                    if (data.getAnnualBudget() != null) existing.setAnnualBudget(data.getAnnualBudget());
                    if (data.getActive() != null) existing.setActive(data.getActive());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setUpdatedBy(data.getUpdatedBy());
                    return repository.save(existing);
                });
    }

    public Mono<Void> softDelete(UUID id) {
        return repository.findById(id)
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .then();
    }

    public Mono<Void> restore(UUID id) {
        return repository.findById(id)
                .flatMap(entity -> {
                    entity.setActive(true);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .then();
    }

    public Mono<Void> hardDeleteById(UUID id) {
        return repository.deleteById(id);
    }

    public Mono<Void> hardDeleteByCode(String code) {
        return repository.deleteByAreaCode(code);
    }
}

