package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.PhysicalLocation;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.PhysicalLocationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhysicalLocationService {

    private final PhysicalLocationRepository repository;

    public Flux<PhysicalLocation> getAll() {
        return repository.findAll();
    }

    public Flux<PhysicalLocation> getAllActive() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    public Flux<PhysicalLocation> getAllInactive() {
        return repository.findAllByActiveFalseOrderByNameAsc();
    }

    public Mono<PhysicalLocation> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("PhysicalLocation not found with id: " + id)));
    }

    public Mono<PhysicalLocation> create(PhysicalLocation entity) {
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public Mono<PhysicalLocation> update(UUID id, PhysicalLocation data) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (data.getLocationCode() != null) existing.setLocationCode(data.getLocationCode());
                    if (data.getName() != null) existing.setName(data.getName());
                    if (data.getDescription() != null) existing.setDescription(data.getDescription());
                    if (data.getLocationType() != null) existing.setLocationType(data.getLocationType());
                    if (data.getParentLocationId() != null) existing.setParentLocationId(data.getParentLocationId());
                    if (data.getAddress() != null) existing.setAddress(data.getAddress());
                    if (data.getFloor() != null) existing.setFloor(data.getFloor());
                    if (data.getSector() != null) existing.setSector(data.getSector());
                    if (data.getReference() != null) existing.setReference(data.getReference());
                    if (data.getGpsCoordinates() != null) existing.setGpsCoordinates(data.getGpsCoordinates());
                    if (data.getMaxCapacity() != null) existing.setMaxCapacity(data.getMaxCapacity());
                    if (data.getAreaM2() != null) existing.setAreaM2(data.getAreaM2());
                    if (data.getResponsibleId() != null) existing.setResponsibleId(data.getResponsibleId());
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
        return repository.deleteByLocationCode(code);
    }
}

